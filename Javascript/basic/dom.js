/*===================== Document Object Model (DMO) Javascript -> Lester E =====================*/

/*========== Selecting Elements ==========*/
document.getElementById("id"); // Selects an element by ID
document.getElementsByClassName("class"); // Selects all elements with a specific class
document.getElementsByTagName("tag"); // selects all elements with a specific tag
document.querySelector("selector"); // Selects the first element that matcher a CSS selector
document.querySelectorAll("selector"); // Selects all elements matching a CSS selector

let element = document.querySelector("selector");
let parentElement = document.querySelector(".parentElement");
let childElement = document.querySelector(".childElement");
let targetElement = document.querySelector(".targetElement");

/*========== Manipulating Elements ==========*/
element.innerHTML = "New Content"; // Changes the inner HTML of an element
element.style.color = "blue"; // Change the CSS style of an element
element.classList.add("newClass"); // Adds a CSS class to an element
element.classList.remove("oldClass"); // Removes a CSS class from an element
element.setAttribute("attribute", "value"); // Sets a new attribute for an element
element.getAttribute("attribute"); // Gets the value of an attribute
element.removeAttribute("attribute"); // Removes an attribute from an element

/*========== Creating and Inserting Elements ==========*/
let newElement = document.createElement("div"); // Creates a new element
document.body.appendChild(newElement); // Adds the new element to the end of <body>
document.body.insertBefore(newElement, targetElement); // Inserts before a specific element
element.appendChild(newElement); // Appends a child element to a parent

/*========== Removing Elements ==========*/
element.remove(); // Removes an element from the DOM
parentElement.removeChild(childElement); // Removes a specific child from a parent

/*========== Traversing the DMO ==========*/
element.parentNode; // Accesses the parent node of an element
element.children; // Returns a live collection of child elements
element.firstChild; // Accesses the first child node (including text)
element.firstElementChild; // Accesses the first child element (including text nodes)
element.lastChild; // Accesses the last child node
element.lastElementChild; // Accesses the last child element

/*========== Events ==========*/
element.addEventListener("click", function () { /* Code */ }); // Adds an event listener
element.removeEventListener("click", function() { /* Code */ }); // Removes an event listener
element.onclick = function () { /* Code */ }; // Sets an onclick event directly

/*========== Miscellaneous ==========*/
document.title = "New Title"; // Sets the document title
document.body.style.backgroundColor = "lightgrey"; // Changes background color of <body>
window.scrollTo(0, 0); // Scrolls the window to the top