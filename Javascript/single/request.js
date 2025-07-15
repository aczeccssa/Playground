const url = "https://ergast.com/api/f1/drivers.json"
fetch(url).then(res => res.json()).then(console.log)