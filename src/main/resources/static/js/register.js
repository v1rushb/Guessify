async function signUp() {
    const name = document.getElementById('signup-name').value.trim();
    const password = document.getElementById('signup-password').value.trim();
    const bio = document.getElementById('signup-bio').value.trim();
    const age = document.getElementById('signup-age').value.trim();

    if (!name || name.length < 2 || name.length > 50) {
        alert("Name must be between 2 and 50 characters.");
        return;
    }

    const passwordRegex = /^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-={}:;"'<>?,./]).*$/;
    if (!password || password.length < 8 || !passwordRegex.test(password)) {
        alert("Password must be at least 8 characters, contain one uppercase letter , and one special character.");
        return;
    }

    const userData = {
        name: name,
        password: password,
        bio: bio,
        age: age
    };

    try {
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.status === 409) {
            throw new Error('Username already exists. Please choose another one.');
        }
        if (!response.ok) {
            const errorMessage = await response.text();
            throw new Error(`Failed to sign up: ${errorMessage}`);
        }

        alert('Sign up successful!');
        await redirectToSignIn();

    } catch (error) {
        console.error('Error during sign-up:', error);
        const errorMessageDiv = document.getElementById('error-message');
        errorMessageDiv.style.display = 'block';
        errorMessageDiv.innerText = error.message;
    }
}



async function redirectToSignIn() {
    try {
        const response = await fetch('/', {
            method: 'GET',
            headers: {
                'Content-Type': 'text/html'
            }
        });

        if (response.ok) {
            window.location.href = '/';
        } else {
            throw new Error(`Error: ${response.status}`);
        }
    } catch (error) {
        console.error("Error redirecting to sign-in:", error);
        alert("Unable to redirect to the sign-in page. Please try again later.");
    }
}
