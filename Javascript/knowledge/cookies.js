const data = [{
    username: "Lester E",
    age: "18"
}, {
    username: "Lnana",
    age: "19"
}];

const cookies = globalThis.cookie= "userList=" + JSON.stringify(data);
const getDataStr = globalThis.cookie.match(`(^| )userList=([^;]+)`)[2];
const resourceData = JSON.parse(getDataStr);

console.assert(resourceData === data);