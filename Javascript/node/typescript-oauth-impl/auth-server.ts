import express from 'express';
import bodyParser from 'body-parser';
import jwt from 'jsonwebtoken';
import { v4 as uuid } from 'uuid';
import fs from 'fs/promises';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import cors from 'cors';
import https from 'https';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const usersRecordPath = path.join(__dirname, 'users.json');

const app = express();
app.use(cors({ origin: "*" })); // Allow all origins
app.use(bodyParser.json());

const securityKey = "security-key"

// Simulated user database
const users = new Array<{ id: string, username: string, password: string }>()

// Generate JWT token for user login
const generateToken = (user: any) => {
  return jwt.sign({
    userId: user.id,
    username: user.username
  }, securityKey, {
    expiresIn: '1h'
  });
};

// Login endpoint
app.post('/login', (req, res) => {
  const { username, password } = req.body;
  const user = users.find(u => u.username === username && u.password === password);

  if (user) {
    const token = generateToken(user);
    res.json({ token });
  } else {
    res.status(401).json({ message: 'Invalid credentials' });
  }
});

app.post('/register', (req, res) => {
  const { username, password } = req.body;
  const duplicateUser = users.find(u => u.username === username && u.password === password);
  if (duplicateUser) {
    res.status(400).json({ message: 'Username already exists' });
  }
  const user = { id: uuid(), username, password }
  users.push(user);
  const token = generateToken(user);
  res.status(201).json({ token });
});

// Token verification middleware for protected routes
const verifyToken = (req: any, res: any, next: any) => {
  const token = req.headers['authorization'];

  if (!token) {
    return res.status(403).json({ message: 'No token provided' });
  }

  jwt.verify(token, securityKey, (err: any, payload: any) => {
    if (err) {
      return res.status(401).json({ message: 'Unauthorized' });
    }
    req.user = payload;
    next();
  });
};

app.get('/info', verifyToken, (req, res) => {
  try {
    const payload = req["user"];
    res.status(200).json({
      id: payload["userId"],
      username: payload["username"],
      token: req.headers['authorization']
    });
  } catch (err) {
    res.status(401).json({ message: 'Unauthorized' });
  }
})

async function saveUsers() {
  try {
    console.log('Users data before stringify:', users);
    const data = JSON.stringify(users, null, 4); // Format object to string and using indent 4
    console.log('Stringified data:', data);

    await fs.writeFile(usersRecordPath, data, { flag: 'w' });

    console.log('Cleanup performed successfully');
  } catch (error) {
    console.error('Error during cleanup:', error);
  }
}

async function loadUsers() {
  try {
    const data = await fs.readFile(usersRecordPath, 'utf8');
    const list = JSON.parse(data);

    list.forEach((user: { id: string; username: string; password: string; }) => {
      users.push(user);
    });

    console.log('Users loaded successfully');
  } catch (error) {
    // Process file not found or JSON parsing error
    if (error.code === 'ENOENT') {
      console.log('User file not found, starting with empty list');
    } else {
      console.error('Error loading users:', error);
    }
  }
}

async function main() {
  const options = {
    key: await fs.readFile('./ca/server.private.key'),
    cert: await fs.readFile('./ca/server.crt')
  }

  const port = 4000;
  loadUsers();
  var httpsServer = https.createServer(options, app);
  const server = httpsServer.listen(port, () => {
    console.log(`Auth server running on port ${port}`);
  });

  // Watch for process termination signals
  process.on('SIGINT', async () => {
    console.log('Received SIGINT. Closing server...');

    // Close the server gracefully, then perform cleanup and exit the process
    server.close(async () => {
      console.log('Server closed');

      // Execute cleanup tasks
      await saveUsers();

      // Exit the process
      process.exit(0);
    });
  });

  process.on('SIGTERM', async () => {
    console.log('Received SIGTERM. Closing server...');

    server.close(async () => {
      console.log('Server closed');
      await saveUsers();
      process.exit(0);
    });
  });
}

main();