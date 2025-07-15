import axios from 'axios';
import https from 'https';

const request = axios.create({
    baseURL: 'https://frontend.lifemark-next.orb.local/api/',
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

request.interceptors.response.use(
    response => {
        if (response.status >= 300 || response.status < 100) {
            Promise.reject(new Error('Request failed with status: ' + response.status));
        }
        return response.data;
    },
    error => {
        console.error('Error during request:', error);
        return Promise.reject(error);
    }
);

async function main() {
    try {
        let token = null; // Global variable token

        // Make the login request
        const credentials = await request({
            method: 'POST',
            url: 'login/blind',
            data: { username: 'example', password: '123456Aa_' }
        });
        // Validate the response and extract the token
        if (credentials['main']['token']) {
            token = credentials['main']['token'];
        }

        // Set the Authorization header with the token
        request.defaults.headers.common['Authorization'] = `Bearer ${token}`;

        // Make the request to get user information
        const userInfo = await request({
            method: 'GET',
            url: 'user/information',
        });
        if (userInfo['main']) {
            console.table(userInfo['main']);
        } else {
            console.log(userInfo);
        }
    } catch (error) {
        console.error('Error during main:', error);
    }
}

main().then(() => {
    console.log('Main function executed successfully');
});