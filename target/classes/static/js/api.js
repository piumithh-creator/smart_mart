// =========================================
// API Module - Handles all HTTP requests
// =========================================

const API_BASE_URL = 'http://localhost:8080/api';

// Store the JWT token
let token = localStorage.getItem('smartmart_token');
let currentUser = JSON.parse(localStorage.getItem('smartmart_user'));

// Helper function to make API requests
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultHeaders = {
        'Content-Type': 'application/json',
    };
    
    // Add auth token if available
    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(url, {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    });
    
    const data = await response.json();
    
    if (!response.ok) {
        throw {
            message: data.message || 'An error occurred',
            status: response.status,
            data: data
        };
    }
    
    return data;
}

// Auth functions
const Auth = {
    // Login user
    async login(email, password) {
        return await apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },
    
    // Register user
    async register(userData) {
        return await apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },
    
    // Logout user
    logout() {
        token = null;
        currentUser = null;
        localStorage.removeItem('smartmart_token');
        localStorage.removeItem('smartmart_user');
    },
    
    // Check if user is authenticated
    isAuthenticated() {
        return !!token;
    },
    
    // Check if user is admin
    isAdmin() {
        return currentUser?.roles?.includes('ROLE_ADMIN') || false;
    },
    
    // Get current user
    getCurrentUser() {
        return currentUser;
    },
    
    // Set auth state
    setAuthState(accessToken, user) {
        token = accessToken;
        currentUser = user;
        localStorage.setItem('smartmart_token', accessToken);
        localStorage.setItem('smartmart_user', JSON.stringify(user));
    }
};

// Product functions
const Product = {
    // Get all products
    async getAll(page = 0, size = 10, sortBy = 'id', categoryId = null, minPrice = null, maxPrice = null) {
        let url = `/products?page=${page}&size=${size}&sortBy=${sortBy}`;
        
        if (categoryId) url += `&categoryId=${categoryId}`;
        if (minPrice) url += `&minPrice=${minPrice}`;
        if (maxPrice) url += `&maxPrice=${maxPrice}`;
        
        return await apiRequest(url);
    },
    
    // Search products
    async search(keyword, page = 0, size = 10) {
        return await apiRequest(`/products/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
    },
    
    // Get product by ID
    async getById(id) {
        return await apiRequest(`/products/${id}`);
    },
    
    // Create product (admin)
    async create(productData) {
        return await apiRequest('/products', {
            method: 'POST',
            body: JSON.stringify(productData)
        });
    },
    
    // Update product (admin)
    async update(id, productData) {
        return await apiRequest(`/products/${id}`, {
            method: 'PUT',
            body: JSON.stringify(productData)
        });
    },
    
    // Delete product (admin)
    async delete(id) {
        return await apiRequest(`/products/${id}`, {
            method: 'DELETE'
        });
    }
};

// Category functions
const Category = {
    // Get all categories
    async getAll() {
        return await apiRequest('/categories');
    },
    
    // Get category by ID
    async getById(id) {
        return await apiRequest(`/categories/${id}`);
    },
    
    // Create category (admin)
    async create(categoryData) {
        return await apiRequest('/categories', {
            method: 'POST',
            body: JSON.stringify(categoryData)
        });
    },
    
    // Update category (admin)
    async update(id, categoryData) {
        return await apiRequest(`/categories/${id}`, {
            method: 'PUT',
            body: JSON.stringify(categoryData)
        });
    },
    
    // Delete category (admin)
    async delete(id) {
        return await apiRequest(`/categories/${id}`, {
            method: 'DELETE'
        });
    }
};

// Cart functions
const Cart = {
    // Get current user's cart
    async getCart() {
        return await apiRequest('/cart');
    },
    
    // Add item to cart
    async addItem(productId, quantity) {
        return await apiRequest('/cart/items', {
            method: 'POST',
            body: JSON.stringify({ productId, quantity })
        });
    },
    
    // Update cart item quantity
    async updateItem(cartItemId, quantity) {
        return await apiRequest(`/cart/items/${cartItemId}`, {
            method: 'PUT',
            body: JSON.stringify({ quantity })
        });
    },
    
    // Remove item from cart
    async removeItem(cartItemId) {
        return await apiRequest(`/cart/items/${cartItemId}`, {
            method: 'DELETE'
        });
    },
    
    // Clear cart
    async clearCart() {
        return await apiRequest('/cart/clear', {
            method: 'DELETE'
        });
    }
};

// Order functions
const Order = {
    // Get current user's orders
    async getMyOrders(page = 0, size = 10) {
        return await apiRequest(`/orders?page=${page}&size=${size}`);
    },
    
    // Get order by ID
    async getById(id) {
        return await apiRequest(`/orders/${id}`);
    },
    
    // Place new order
    async placeOrder(paymentMethod, shippingAddress, notes = '') {
        return await apiRequest('/orders', {
            method: 'POST',
            body: JSON.stringify({
                paymentMethod,
                shippingAddress,
                notes
            })
        });
    },
    
    // Cancel order
    async cancelOrder(id) {
        return await apiRequest(`/orders/${id}`, {
            method: 'DELETE'
        });
    }
};

// Customer functions
const Customer = {
    // Get current user profile
    async getProfile() {
        return await apiRequest('/customers/me');
    },
    
    // Update profile
    async updateProfile(profileData) {
        return await apiRequest('/customers/me', {
            method: 'PATCH',
            body: JSON.stringify(profileData)
        });
    },
    
    // Update customer (admin)
    async update(id, customerData) {
        return await apiRequest(`/customers/${id}`, {
            method: 'PUT',
            body: JSON.stringify(customerData)
        });
    },
    
    // Delete customer (admin)
    async delete(id) {
        return await apiRequest(`/customers/${id}`, {
            method: 'DELETE'
        });
    },
    
    // Get all customers (admin)
    async getAll(page = 0, size = 10) {
        return await apiRequest(`/customers?page=${page}&size=${size}`);
    }
};

// Address functions
const Address = {
    // Get current user's addresses
    async getAddresses() {
        return await apiRequest('/addresses');
    },
    
    // Create address
    async create(addressData) {
        return await apiRequest('/addresses', {
            method: 'POST',
            body: JSON.stringify(addressData)
        });
    },
    
    // Update address
    async update(id, addressData) {
        return await apiRequest(`/addresses/${id}`, {
            method: 'PUT',
            body: JSON.stringify(addressData)
        });
    },
    
    // Delete address
    async delete(id) {
        return await apiRequest(`/addresses/${id}`, {
            method: 'DELETE'
        });
    }
};

// Payment functions
const Payment = {
    // Get payment by order ID
    async getByOrder(orderId) {
        return await apiRequest(`/payments/order/${orderId}`);
    },
    
    // Update payment status (admin)
    async updateStatus(orderId, status) {
        return await apiRequest(`/payments/order/${orderId}`, {
            method: 'PATCH',
            body: JSON.stringify({ paymentStatus: status })
        });
    }
};

// Review functions
const Review = {
    // Get reviews for a product
    async getByProduct(productId, page = 0, size = 10) {
        return await apiRequest(`/reviews/product/${productId}?page=${page}&size=${size}`);
    },
    
    // Create review
    async create(productId, rating, comment) {
        return await apiRequest('/reviews', {
            method: 'POST',
            body: JSON.stringify({ productId, rating, comment })
        });
    },
    
    // Update review
    async update(id, rating, comment) {
        return await apiRequest(`/reviews/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ rating, comment })
        });
    },
    
    // Delete review
    async delete(id) {
        return await apiRequest(`/reviews/${id}`, {
            method: 'DELETE'
        });
    }
};

// Supplier functions
const Supplier = {
    // Get all suppliers
    async getAll() {
        return await apiRequest('/suppliers');
    },
    
    // Get supplier by ID
    async getById(id) {
        return await apiRequest(`/suppliers/${id}`);
    },
    
    // Create supplier (admin)
    async create(supplierData) {
        return await apiRequest('/suppliers', {
            method: 'POST',
            body: JSON.stringify(supplierData)
        });
    },
    
    // Update supplier (admin)
    async update(id, supplierData) {
        return await apiRequest(`/suppliers/${id}`, {
            method: 'PUT',
            body: JSON.stringify(supplierData)
        });
    },
    
    // Delete supplier (admin)
    async delete(id) {
        return await apiRequest(`/suppliers/${id}`, {
            method: 'DELETE'
        });
    }
};

// Admin functions
const Admin = {
    // Get dashboard statistics
    async getDashboard() {
        return await apiRequest('/admin/dashboard');
    },
    
    // Get inventory summary (low stock)
    async getInventorySummary() {
        return await apiRequest('/admin/inventory-summary');
    }
};

// Export all modules
window.SmartMartAPI = {
    Auth,
    Product,
    Category,
    Cart,
    Order,
    Customer,
    Address,
    Payment,
    Review,
    Admin,
    Supplier
};