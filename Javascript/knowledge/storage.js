/*================== Key-Value Pair Storage ==================*/

// 数据结构: key: value
//    1. 通过key获取value
//    2. key和value都是字符串

/*================== LocalStorage ==================*/
// 本地的永久存在的
localStorage.setItem("一句话", "娜娜我最喜欢你啦"); // (key, value)
localStorage.getItem("一句话"); // (key)
localStorage.removeItem("一句话"); // (key)
localStorage.clear(); // 我哄温了, 数据都别活了全删了


/*================== SessionStorage ==================*/
// 会话的暂存的
sessionStorage.setItem("一句话", "娜娜我最喜欢你啦"); // (key, value)
sessionStorage.getItem("一句话"); // (key)
sessionStorage.removeItem("一句话"); // (key)
sessionStorage.clear(); // 我哄温了, 数据都别活了全删了