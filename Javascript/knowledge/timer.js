// 轮询定时器: setInterval(), 轮训定时器清除方法: clearInterval()
// 定时器: setTimeout(), 定时器清除方法: clearIimeout()
// setInterval和setTimeout的参数是一样的:
//     第一个参数: 接受一个回掉函数 function () { /* 代码块 */ }
//     第二个参数: 接受一个数字, 表示延迟的时间, 单位是毫秒
// clearInterval和clearIimeout的参数是一样的:
//     只有一个参数: 定时器(如setXXX的返回值)

// 演示实例:
console.log("前情提要: 娜娜会在轮询器轮询第二遍和第三遍之间的时间内关闭轮询器(拦截).")

let times = 0;
const timeout = 1000;
const interval = setInterval(function () {
    times++;
    console.log(`这个输出是没${timeout}毫秒执行一次的.`);
    if (times === 2 && Math.random() >= 0.5) {
        console.log("娜娜被拦截啦! 任务完成咯~");
        interceptorAll();
    }
}, timeout);

const timer = setTimeout(function () {
    clearInterval(interval); // 清除上面的轮训定时器.
    console.log("定时器没清除掉了, 娜娜说不给你运行.")
}, timeout * 2.5);

function interceptorAll() {
    clearTimeout(timer);
    clearInterval(interval);
}