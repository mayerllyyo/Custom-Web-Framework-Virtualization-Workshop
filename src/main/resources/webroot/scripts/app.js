async function sayHello() {
    const name = document.getElementById('nameInput').value.trim();
    const result = document.getElementById('helloResult');
    try {
        const response = await fetch('/greeting?name=' + encodeURIComponent(name));
        result.textContent = await response.text();
    } catch (e) {
        result.textContent = 'Error: ' + e.message;
    }
}

async function saySimpleHello() {
    const result = document.getElementById('simpleHelloResult');
    try {
        const response = await fetch('/hello');
        result.textContent = await response.text();
    } catch (e) {
        result.textContent = 'Error: ' + e.message;
    }
}

async function getPi() {
    const result = document.getElementById('piResult');
    try {
        const response = await fetch('/pi');
        result.textContent = await response.text();
    } catch (e) {
        result.textContent = 'Error: ' + e.message;
    }
}
