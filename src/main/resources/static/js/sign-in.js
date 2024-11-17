async function signIn() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    if (!username || !password) {
        alert("All fields are required ! ");
        return;
    }

    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name: username, password: password })
        });

        if (response.status === 401) {
            alert("Incorrect username or password. Please try again.");
            return;
        }
        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }
        const data = await response.json();

        localStorage.setItem('username', data.name);
        localStorage.setItem('userId', data.userId);

        await redirectToHome();
    } catch (error) {
        console.error("Error signing in:", error);
        alert("Network error. Please check your connection and try again.");
    }
}

async function redirectToHome() {
    try {
        const response = await fetch('/home', {
            method: 'GET',
            headers: {
                'Content-Type': 'text/html'
            }
        });

        if (response.ok) {
            window.location.href = '/home';
        } else {
            throw new Error(`Error: ${response.status}`);
        }
    } catch (error) {
        console.error("Error redirecting to home:", error);
        alert("Unable to redirect to the home page. Please try again later.");
    }
}
