// 假如这是一个网络请求
function asyncFunc() {
    return new Promise((resolve, reject) => {
        const dealy = Math.random() * 1000;
        setTimeout(() => {
            const failure = Math.random() < 0.07;
            failure ? reject(new Error('请求失败')) : resolve('请求成功');
        }, dealy);
    });
}

function noResAsyncFunc() {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const failure = Math.random() < 0.07;
            failure ? reject() : resolve();
        }, 1000);
    })
}

// Promise -> then() 成功
// Promise -> catch() 错误
// Promise -> finally() 最后
// then/catch -> Promise
// finally -> void 结束啦～

noResAsyncFunc().then(function () {
//     console.log("成功了，没有结果的那种成功");
    throw new Error("失败了")
}).catch((e) => {
        console.error(e);
    }).finally(() => {
        console.log("异步操作完成");
    })


const obj = {

}

