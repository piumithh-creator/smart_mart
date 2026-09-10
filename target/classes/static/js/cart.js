// =========================================
// Cart Module
// =========================================

const CartUI = {
    cart: null,
    
    // Render cart items
    async renderCart() {
        const container = document.getElementById('cart-container');
        const emptyContainer = document.getElementById('cart-empty');
        const summaryContainer = document.getElementById('cart-summary');
        
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }
        
        if (container) container.innerHTML = '<div class="spinner"></div>';
        
        try {
            const response = await SmartMartAPI.Cart.getCart();
            this.cart = response.data;
            
            if (!this.cart || this.cart.cartItems.length === 0) {
                if (emptyContainer) emptyContainer.style.display = 'block';
                if (summaryContainer) summaryContainer.style.display = 'none';
                return;
            }
            
            if (emptyContainer) emptyContainer.style.display = 'none';
            if (summaryContainer) summaryContainer.style.display = 'block';
            
            this.renderCartItems(this.cart.cartItems);
            this.renderCartSummary(this.cart);
            
        } catch (error) {
            if (container) {
                container.innerHTML = `
                    <div class="alert alert-error">
                        <span class="alert-icon">⚠️</span>
                        <span>${error.message}</span>
                    </div>
                `;
            }
        }
    },
    
    // Render cart items list
    renderCartItems(items) {
        const container = document.getElementById('cart-items');
        if (!container) return;
        
        container.innerHTML = items.map((item, index) => `
            <div class="cart-item">
                <img src="${this.getImagePlaceholder()}" alt="${item.productName}" class="cart-item-image">
                <div class="cart-item-details">
                    <div class="cart-item-name">${item.productName}</div>
                    <div class="cart-item-sku">SKU: ${item.productSku}</div>
                    <div class="cart-item-price">$${item.unitPrice.toFixed(2)}</div>
                    <div class="cart-item-quantity">
                        <button class="quantity-btn" onclick="CartUI.updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                        <span>${item.quantity}</span>
                        <button class="quantity-btn" onclick="CartUI.updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
                    </div>
                </div>
                <div class="cart-item-subtotal">
                    <div class="price">$${item.subtotal.toFixed(2)}</div>
                </div>
                <button class="cart-item-remove" onclick="CartUI.removeItem(${item.id})">
                    🗑️
                </button>
            </div>
        `).join('');
    },
    
    // Render cart summary
    renderCartSummary(cart) {
        const container = document.getElementById('cart-summary');
        if (!container) return;
        
        container.innerHTML = `
            <h3>Order Summary</h3>
            <div class="summary-row">
                <span>Subtotal</span>
                <span>$${cart.totalAmount.toFixed(2)}</span>
            </div>
            <div class="summary-row">
                <span>Shipping</span>
                <span>Free</span>
            </div>
            <div class="summary-row">
                <span>Total</span>
                <span>$${cart.totalAmount.toFixed(2)}</span>
            </div>
            <div class="checkout-form" style="margin-top: 20px;">
                <button class="btn btn-primary btn-block" onclick="CartUI.checkout()">
                    Proceed to Checkout
                </button>
            </div>
        `;
    },
    
    // Update cart item quantity
    async updateQuantity(cartItemId, quantity) {
        if (quantity < 1) return;
        
        try {
            const response = await SmartMartAPI.Cart.updateItem(cartItemId, quantity);
            this.cart = response.data;
            this.renderCartItems(this.cart.cartItems);
            this.renderCartSummary(this.cart);
            this.updateCartCount();
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Remove item from cart
    async removeItem(cartItemId) {
        if (!confirm('Remove this item from cart?')) return;
        
        try {
            const response = await SmartMartAPI.Cart.removeItem(cartItemId);
            this.cart = response.data;
            
            if (this.cart.cartItems.length === 0) {
                this.renderCart();
            } else {
                this.renderCartItems(this.cart.cartItems);
                this.renderCartSummary(this.cart);
                this.updateCartCount();
            }
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Checkout
    async checkout() {
        window.location.href = 'checkout.html';
    },
    
    // Update cart count in navbar
    updateCartCount() {
        const count = document.getElementById('cart-count');
        if (count && this.cart) {
            count.textContent = this.cart.totalItems;
        }
    },
    
    // Get image placeholder
    getImagePlaceholder() {
        return 'https://via.placeholder.com/100x100/cccccc/666666?text=No+Image';
    },
    
    // Initialize cart module
    init() {
        this.renderCart();
        
        // Bind cart count click
        const cartLink = document.getElementById('cart-link');
        if (cartLink) {
            cartLink.addEventListener('click', (e) => {
                e.preventDefault();
                window.location.href = 'cart.html';
            });
        }
        
        // Check authentication
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    }
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.CartUI = CartUI;
});