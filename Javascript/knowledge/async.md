# Javascript 异步编程

常见的网络请求都是需要一定的时间的，如果同步请求，那么就会导致页面卡顿，所以需要异步编程。

## 回调函数

回调函数是异步编程中最常用的方式，它可以让函数在异步操作完成后执行。

其实我们写的setTimeout就是一个回调函数，它会在定时器结束后执行。

```javascript
setTimeout(() => {
    console.log('定时器结束');
}, 1000);
```

## Promise

Promise 是异步编程的一种解决方案，它可以让异步操作变得更加简单和直观。

Promise 有三种状态：

- pending：初始状态，既不是成功，也不是失败。
- fulfilled：成功状态。
- rejected：失败状态。

Promise 的实例有三个方法：

- then：用于处理成功状态的回调函数。
- catch：用于处理失败状态的回调函数。
- finally：用于处理无论成功还是失败都会执行的回调函数。

Promise 的实例有三个方法：

- resolve：用于将 Promise 的状态变为成功。
- reject：用于将 Promise 的状态变为失败。
- all：用于将多个 Promise 实例包装成一个新的 Promise 实例。
- race：用于将多个 Promise 实例包装成一个新的 Promise 实例。

## async/await

async/await 是异步编程的另一种解决方案，它可以让异步操作变得更加简单和直观。

async/await 是 Promise 的语法糖，它可以让异步操作变得更加简单和直观。

async/await 的语法糖，它可以让异步操作变得更加简单和直观。

```javascript
async function getData() {
    const res = await fetch('https://api.github.com');
    const data = await res.json();
    console.log(data);
}
```

## 总结

- 回调函数是异步编程中最常用的方式，它可以让函数在异步操作完成后执行。
- Promise 是异步编程的一种解决方案，它可以让异步操作变得更加简单和直观。
- async/await 是异步编程的另一种解决方案，它可以让异步操作变得更加简单和直观。
