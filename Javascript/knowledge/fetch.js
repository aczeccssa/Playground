;(async function () {
    const loginStart = performance.now();
    const loginRes = await fetch("http://localhost:8080/login/blind", {
        body: JSON.stringify({
            "username": "example",
            "password": "123456Aa_"
        }),
        headers: new Headers({
            'Content-Type': 'application/json'
        }),
        method: "POST",
    });
    console.log(`Login request response status is: ${loginRes.status}, using ${Math.round(performance.now() - loginStart)} ms`);
    const loginBody = await loginRes.json();
    if (!(loginBody.main && loginBody.main.token) || loginBody.error) {
        return console.error(new Error(loginBody.error?.message || "Failed with unknown error."));
    }
    const token = loginBody.main.token;
    const infoStart = performance.now();
    const infoRes = await fetch("http://localhost:8080/user/information", {
        headers: new Headers({
            "Authorization": `Bearer ${token}`,
        }),
        method: "GET"
    });
    console.log(`User info request response status is: ${loginRes.status}, using ${Math.round(performance.now() - infoStart)} ms`);
    const infoBody = await infoRes.json();
    if (!infoBody.main || infoBody.error) {
        return console.error(new Error(infoBody.error?.message || "Failed with unknown error."));
    }
    console.table(infoBody.main);
}());