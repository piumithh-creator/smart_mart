// =========================================
// Authentication Module
// =========================================

const AuthUI = {
    // Show error message
    showError(message) {
        const container = document.getElementById('error-message');
        if (container) {
            container.textContent = message;
            container.style.display = 'block';
        }
    },
    
    // Clear error message
    clearError() {
        const container = document.getElementById('error-message');
        if (container) {
            container.style.display = 'none';
        }
    },
    
    // Show success message
    showSuccess(message) {
        const container = document.getElementById('success-message');
        if (container) {
            container.textContent = message;
            container.style.display = 'block';
        }
    },
    
    // Clear success message
    clearSuccess() {
        const container = document.getElementById('success-message');
        if (container) {
            container.style.display = 'none';
        }
    },
    
    // Handle login
    async handleLogin(event) {
        event.preventDefault();
        this.clearError();
        this.clearSuccess();
        
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        
        if (!email || !password) {
            this.showError('Please fill in all fields');
            return;
        }
        
        const btn = document.getElementById('login-btn');
        const originalText = btn.textContent;
        btn.textContent = 'Logging in...';
        btn.disabled = true;
        
        try {
            const response = await SmartMartAPI.Auth.login(email, password);
            
            // Store auth state
            SmartMartAPI.Auth.setAuthState(
                response.data.accessToken,
                {
                    userId: response.data.userId,
                    email: response.data.email,
                    firstName: response.data.firstName,
                    lastName: response.data.lastName,
                    roles: response.data.roles
                }
            );
            
            // Show success and redirect
            this.showSuccess('Login successful! Redirecting...');
            setTimeout(() => {
                const user = SmartMartAPI.Auth.getCurrentUser();
                if (user && user.roles.includes('ROLE_ADMIN')) {
                    window.location.href = 'admin/dashboard.html';
                } else {
                    window.location.href = 'index.html';
                }
            }, 1000);
            
        } catch (error) {
            this.showError(error.message || 'Login failed. Please check your credentials.');
        } finally {
            btn.textContent = originalText;
            btn.disabled = false;
        }
    },
    
    // Handle registration
    async handleRegister(event) {
        event.preventDefault();
        this.clearError();
        this.clearSuccess();
        
        const firstName = document.getElementById('firstName').value.trim();
        const lastName = document.getElementById('lastName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const confirmPassword = document.getElementById('confirmPassword').value.trim();
        const phone = document.getElementById('phone').value.trim();
        
        // Validation
        if (!firstName || !lastName || !email || !password) {
            this.showError('Please fill in all required fields');
            return;
        }
        
        if (password !== confirmPassword) {
            this.showError('Passwords do not match');
            return;
        }
        
        if (password.length < 6) {
            this.showError('Password must be at least 6 characters');
            return;
        }
        
        const btn = document.getElementById('register-btn');
        const originalText = btn.textContent;
        btn.textContent = 'Registering...';
        btn.disabled = true;
        
        try {
            const response = await SmartMartAPI.Auth.register({
                firstName,
                lastName,
                email,
                password,
                phone
            });
            
            // Show success and redirect
            this.showSuccess('Registration successful! You can now login.');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
            
        } catch (error) {
            this.showError(error.message || 'Registration failed. Please try again.');
        } finally {
            btn.textContent = originalText;
            btn.disabled = false;
        }
    },
    
    // Handle logout
    handleLogout() {
        SmartMartAPI.Auth.logout();
        window.location.href = 'index.html';
    },
    
    // Check authentication on page load
    checkAuth() {
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    },
    
    // Check admin access
    checkAdminAccess() {
        if (!SmartMartAPI.Auth.isAdmin()) {
            window.location.href = 'index.html';
        }
    },
    
    // Render navigation
    renderNavbar() {
        const navbarLinks = document.getElementById('navbar-links');
        const authLinks = document.getElementById('auth-links');
        
        if (SmartMartAPI.Auth.isAuthenticated()) {
            const user = SmartMartAPI.Auth.getCurrentUser();
            
            // Show user-specific links
            if (authLinks) {
                authLinks.innerHTML = `
                    <li class="nav-item">
                        <a class="nav-link" href="profile.html">
                            <span class="user-avatar">${user.firstName.charAt(0)}</span>
                            ${user.firstName}
                        </a>
                    </li>
                    <li class="nav-item">
                        <button class="btn btn-outline" onclick="AuthUI.handleLogout()">Logout</button>
                    </li>
                    <li class="nav-item">
                        ${SmartMartAPI.Auth.isAdmin() ? 
                            '<a class="nav-link" href="admin/dashboard.html">Admin</a>' : ''}
                    </li>
                `;
            }
            
            // Update cart link
            if (window.CartUI) {
                window.CartUI.updateCartCount();
            }
            
        } else {
            // Show guest links
            if (authLinks) {
                authLinks.innerHTML = `
                    <li class="nav-item">
                        <a class="nav-link" href="login.html">Login</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="register.html">Register</a>
                    </li>
                `;
            }
        }
    },
    
    // Initialize auth module
    init() {
        this.renderNavbar();
        
        // Login form
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }
        
        // Register form
        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }
    }
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    AuthUI.init();
});