// ObservableList.ts

type ObserverCallback<T extends Array<any>> = (array: T) => void;

class ObservableList<T> {
    constructor(...args: Array<T>) {
        this.list = args;
    }

    /*================== PRIVATE API ==================*/

    private readonly list: Array<T> = new Array<T>();

    private readonly observers: Array<ObserverCallback<Array<T>>> = new Array<ObserverCallback<Array<T>>>();

    private notify(): void {
        this.observers.forEach((observer) => {
            observer(this.list);
        });
    }

    /*================== PUBLIC API: METHOD ==================*/

    public subscribe(observer: ObserverCallback<Array<T>>): void {
        this.observers.push(observer);
    }

    public unsubscribe(observer: ObserverCallback<Array<T>>): void {
        this.observers.splice(this.observers.indexOf(observer), 1);
    }

    public push(value: T): void {
        this.list.push(value);
        this.notify();
    }

    public remove(value: T): void {
        this.list.splice(this.list.indexOf(value), 1);
        this.notify();
    }

    public pop(): void {
        this.list.pop();
        this.notify();
    }

    public clear(): void {
        this.list.length = 0;
        this.notify();
    }

    public forEach(callbackFn: (value: T, index: number, array: T[]) => void, thisArg?: any): void {
        this.list.forEach(callbackFn, thisArg);
    }

    public includes(searchElement: T, fromIndex?: number): boolean {
        let res = true;
        this.list.slice(0, fromIndex || this.list.length - 1).forEach((item) => res = item === searchElement);
        return res;
    }

    public get(i: number): T {
        return this.list[i];
    }

    /*================== PUBLIC API: CALCULATION PROPERTY ==================*/

    public get length(): number {
        return this.list.length;
    }
}


/*================== Observable List Instance && E.G. ==================*/
const obList = new ObservableList<string>();

// Add observer
obList.subscribe(li => console.debug("Observed list change: list size is ", li.length));

// @Mark Test API: push value
const value1 = "1234567890"
obList.push("0987654321");
obList.push("1234567890");

// @Mark Test API: include value
console.log(`Is that list include ${value1}: ${obList.includes(value1)}`);

// @Mark Test API: Init using args and get list length
console.log(new ObservableList(1, 2, 3, 4).length);