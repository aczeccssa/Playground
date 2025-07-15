// ===========================
// 1) Module Pattern
// ===========================
const Module = (function(){
    let privateVar = "I am private"; // Private variable

    // Private method
    function privateMethod() {
        console.log(privateVar);
    }

    // Public API
    return {
        publicMethod: function () {
            privateMethod();
        }
    };
})();

Module.publicMethod(); // Output: I am private


// ===========================
// 2) Singleton Pattern
// ===========================
const Singleton = (function(){
    let instance;

    // Create instance if it doesn't exist
    function createInstance() {
        return { name: "Singleton Instance" }; // Instance object
    }

    return {
        getInstance: function () {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();

const instance1 = Singleton.getInstance();
const instance2 = Singleton.getInstance();
console.log(instance1 === instance2); // Output: true


// ===========================
// 3) Factory Pattern
// ===========================
function Car(make, model) {
    this.make = make; // Car make
    this.model = model; // Car model
}

function CarFactory() {
    return {
        createCar: function (make, model) {
            return new Car(make, model); // Create new Car instance
        }
    };
}

const factory = new CarFactory();
const car1 = factory.createCar("Toyota", "Corolla");
console.log(car1); // Output: Car { make: "Toyota", model: "Corolla" }


// ===========================
// 4) Observer Pattern
// ===========================
function Subject() {
    const observers = []; // List of observers

    return {
        subscribe: function (observer) {
            observers.push(observer); // Add observer
        },
        notify: function (data) {
            observers.forEach(observer => observer(data)) // Notify all observers
        }
    };
}

const subject = new Subject();
subject.subscribe(data => console.log("Observer 1: ", data)); // Observer 1 callback
subject.subscribe(data => console.log("Observer 2: ", data)); // Observer 2 callback
subject.notify("Hello Observer!"); // Output: Observer 1: Hello Observer! / Observer 2: Hello Observer!


// ===========================
// 5) Decorator Pattern
// ===========================
function Coffee() {
    this.cost = function () {
        return 5; // Base cost of coffee
    };
}

function MilkDecorator(coffee) {
    this.cost = function () {
        return coffee.cost() + 2; // Add cost of milk
    };
}

const coffee = new Coffee();
const milkCoffee = new MilkDecorator(coffee);
console.log(milkCoffee.cost()); // Output: 7


// ===========================
// 6) Command Pattern
// ===========================
class Command {
    constructor(receiver) {
        this.receiver = receiver; // Command receiver
    }

    execute() {
        this.receiver.action();
    }
}

class Receiver {
    action() {
        console.log("Action executed!"); // Action to be executed
    }
}

const receiver = new Receiver();
const command = new Command(receiver);
command.execute(); // Output: Action executed!


// ===========================
// 7) Prototype Pattern
// ===========================
const carPrototype = {
    driver() {
        console.log("Vroom Vroom!"); // Method on prototype
    }
};

const car = Object.create(carPrototype); // Create new object base on prototype
car.driver(); // Output: Vroom Vroom!