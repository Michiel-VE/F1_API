console.log("Custom Swagger UI config loaded!");

window.onload = function() {
    console.log("Swagger UI custom configuration is being loaded.");

    const ui = SwaggerUIBundle({
        url: "/api/v1/api-docs",  // Your OpenAPI docs path
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.presets.deepLinking],
        layout: "BaseLayout",
        onComplete: function() {
            console.log("Swagger UI initialized successfully!");

            // Add custom logic for the "Authorize" button
            const authButton = document.querySelector('.authorize');
            if (authButton) {
                authButton.addEventListener('click', function() {
                    console.log("Authorize button clicked.");

                    // Collect username and password from the user
                    const username = prompt('Enter your username');
                    const password = prompt('Enter your password');

                    // Call the backend login API to get the JWT token
                    fetch('/api/v1/auth/login?username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(password), {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                    })
                        .then(response => response.json())
                        .then(data => {
                            console.log("Token received: ", data.token);
                            const token = data.token;
                            // Now set the Authorization header in Swagger UI
                            ui.preauthorizeApiKey('bearerAuth', token);
                            alert('JWT token set successfully!');
                        })
                        .catch(err => {
                            alert('Error during authentication: ' + err.message);
                        });
                });
            }
        }
    });

    window.ui = ui;
};
