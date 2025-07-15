import axios from 'axios';
import bodyParser from 'body-parser';
import WebSocket, { WebSocketServer } from 'ws';
import express from 'express';
import cors from 'cors';
import { URLSearchParams } from 'url';
import https from 'https';
import fs from 'fs/promises';

const app = express();
app.use(cors({ origin: "*" })); // Allow all origins
app.use(bodyParser.json());

const request = axios.create({
  baseURL: 'https://localhost:4000/',
  headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
  },
  // Allow unsafe HTTPS connections
  httpsAgent: new https.Agent({
      rejectUnauthorized: false,
  }),
  timeout: 10000,
});

const getUserInfo = async function (token: string): Promise<object | null> {
  try {
    console.log('Fetching user info with token:', token);
    const res = await request.get('https://localhost:4000/info', {
      headers: {
        'authorization': token
      }
    });
    if (res.data["token"]) {
      return res.data; // { id: string, username: string, token: string  }
    } else {
      return null;
    }
  } catch (error) {
    console.error('Error fetching user info:', error.message);
    return null;
  }
}

function configureWebSocketServer(wss: WebSocketServer) {
  // Using Map to storage connection information, key is userID, value is WebSocket instance
  const websocketConnectionPool = new Map<string, { socket: WebSocket, username: string }>();

  wss.on('connection', async (ws, req) => {
    let token: string | null = null;

    if (req.url) {
      const queryString = req.url.split('?')[1];
      const urlParams = new URLSearchParams(queryString);
      token = urlParams.get('authorization');
    }

    if (!token) {
      console.log('No JWT token provided');
      ws.close(1008, 'Authentication failed: Missing token');
      return;
    }

    // Get user information
    let userInfo: object | null = null;
    try {
      userInfo = await getUserInfo(token);
    } catch (error) {
      console.error('Error retrieving user info:', error);
      ws.close(1008, 'Authentication failed: Server error');
      return;
    }

    if (!userInfo) {
      console.log('Invalid user token');
      ws.close(1008, 'Authentication failed: Invalid credentials');
      return;
    }

    const userId = userInfo["id"];

    // Validate user connection status
    if (websocketConnectionPool.has(userId)) {
      console.log('User already connected:', userId);
      ws.close(1008, 'Authentication failed: Already connected');
      return;
    }

    // Save connection information
    websocketConnectionPool.set(userId, { socket: ws, username: userInfo["username"] });
    console.log(`${userInfo["username"]} (${userId}) connected`);

    // Broadcast message to all connected clients
    const broadcast = (message: string) => {
      if (wss.clients.size === 0) {
        console.log('No clients connected');
        return;
      }

      wss.clients.forEach((client) => {
        if (client !== ws && client.readyState === WebSocket.OPEN) {
          client.send(JSON.stringify({ userId, username: userInfo["username"], message }));
        }
      });
    };

    ws.on('message', (data) => {
      console.log(`Received message from ${userInfo["username"]}: ${data}`);
      try {
        const message = JSON.parse(data.toString());
        broadcast(message["message"]);
      } catch (error) {
        console.error('Invalid message format:', error);
        ws.send(JSON.stringify({ error: 'Invalid message format' }));
      }
    });

    ws.on('close', () => {
      // Remove user from connection pool
      websocketConnectionPool.delete(userId);
      console.log(`${userInfo["username"]} (${userId}) disconnected`);
    });
  });
}

app.use(express.static("./static"));

async function main() {
  const options = {
    key: await fs.readFile('./ca/server.private.key'),
    cert: await fs.readFile('./ca/server.crt')
  };

  const port = 3000;
  const httpsServer = https.createServer(options, app); // Using https server

  // Bind WebSocket to HTTPS server
  const wss = new WebSocketServer({ server: httpsServer, path: "/chat" });
  configureWebSocketServer(wss);

  httpsServer.listen(port, () => {
    console.log(`Business server running on port ${port}`);
  });
}

main();