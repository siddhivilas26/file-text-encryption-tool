// Simple XOR Encryption for Demo
function xorEncryptDecrypt(input, key) {
    let output = "";
    for (let i = 0; i < input.length; i++) {
        output += String.fromCharCode(input.charCodeAt(i) ^ key.charCodeAt(i % key.length));
    }
    return output;
}

// Add to history
function addToHistory(action, content) {
    const historyList = document.getElementById("history");
    const li = document.createElement("li");
    li.textContent = `${action}: ${content.substring(0, 30)}...`;
    historyList.appendChild(li);
}

// Encrypt Text
function encryptText() {
    const text = document.getElementById("textInput").value;
    const password = document.getElementById("password").value;
    if (!text || !password) {
        alert("Enter both text and password!");
        return;
    }
    const encrypted = btoa(xorEncryptDecrypt(text, password));
    document.getElementById("result").textContent = encrypted;
    addToHistory("Encrypted Text", text);
}

// Decrypt Text
function decryptText() {
    const text = document.getElementById("textInput").value;
    const password = document.getElementById("password").value;
    if (!text || !password) {
        alert("Enter both text and password!");
        return;
    }
    try {
        const decrypted = xorEncryptDecrypt(atob(text), password);
        document.getElementById("result").textContent = decrypted;
        addToHistory("Decrypted Text", decrypted);
    } catch (e) {
        alert("Invalid encrypted text or password!");
    }
}

// Encrypt File (Simulation)
function encryptFile() {
    const filename = document.getElementById("filename").value;
    const password = document.getElementById("password").value;
    if (!filename || !password) {
        alert("Enter filename and password!");
        return;
    }
    const encrypted = btoa(xorEncryptDecrypt(filename, password));
    document.getElementById("result").textContent = `Encrypted File Content: ${encrypted}`;
    addToHistory("Encrypted File", filename);
}

// Decrypt File (Simulation)
function decryptFile() {
    const filename = document.getElementById("filename").value;
    const password = document.getElementById("password").value;
    if (!filename || !password) {
        alert("Enter filename and password!");
        return;
    }
    try {
        const decrypted = xorEncryptDecrypt(atob(btoa(filename)), password);
        document.getElementById("result").textContent = `Decrypted File Content: ${decrypted}`;
        addToHistory("Decrypted File", filename);
    } catch (e) {
        alert("Invalid file or password!");
    }
}

// Dark Mode Toggle
document.getElementById("toggleTheme").addEventListener("click", function () {
    document.body.classList.toggle("dark-mode");
    this.textContent = document.body.classList.contains("dark-mode") ? "☀️" : "🌙";
});
