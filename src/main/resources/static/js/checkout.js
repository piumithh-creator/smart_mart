// =========================================
// Checkout Module
// =========================================

const CheckoutUI = {
    cart: null,
    addresses: [],
    
    // Initialize checkout
    async init() {
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }
        
        await this.loadCart();
        await this.loadAddresses();
        this.bindEvents();
    },
    
    // Load cart data
    async loadCart() {
        try {
            const response = await SmartMartAPI.Cart.getCart();
            this.cart = response.data;
            
            if (!this.cart || this.cart.cartItems.length === 0) {
                this.showError('Your cart is empty');
                setTimeout(() => {
                    window.location.href = 'products.html';
                }, 2000);
                return;
            }
            
            this.renderCartItems(this.cart.cartItems);
            this.renderOrderSummary(this.cart);
            
        } catch (error) {
            this.showError(error.message);
        }
    },
    
    // Load addresses
    async loadAddresses() {
        try {
            const response = await SmartMartAPI.Address.getAddresses();
            this.addresses = response.data;
            this.renderAddressSelector(this.addresses);
        } catch (error) {
            // Addresses might not be available yet
            this.addresses = [];
        }
    },
    
    // Render cart items
    renderCartItems(items) {
        const container = document.getElementById('cart-items-preview');
        if (!container) return;
        
        container.innerHTML = items.map(item => `
            <div style="padding: 1rem 0; border-bottom: 1px solid #e5e7eb;">
                <div style="font-weight: 600;">${item.productName}</div>
                <div>Qty: ${item.quantity} × $${item.unitPrice.toFixed(2)}</div>
                <div style="font-weight: bold; color: #2563eb; margin-top: 0.5rem;">$${item.subtotal.toFixed(2)}</div>
            </div>
        `).join('');
    },
    
    // Render order summary
    renderOrderSummary(cart) {
        const container = document.getElementById('order-summary');
        if (!container) return;
        
        container.innerHTML = `
            <div style="padding: 1rem 0; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between;">
                <span>Subtotal</span>
                <span>$${cart.totalAmount.toFixed(2)}</span>
            </div>
            <div style="padding: 1rem 0; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between;">
                <span>Shipping</span>
                <span>Free</span>
            </div>
            <div style="padding: 1rem 0; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; font-weight: bold; border-top: 2px solid #2563eb; margin-top: 1rem; padding-top: 1rem;">
                <span>Total</span>
                <span>$${cart.totalAmount.toFixed(2)}</span>
            </div>
        `;
    },
    
    // Render address selector
    renderAddressSelector(addresses) {
        const container = document.getElementById('address-select');
        if (!container) return;
        
        if (addresses.length === 0) {
            container.innerHTML = `
                <p>You haven't added any addresses yet.</p>
                <button class="btn btn-primary" onclick="CheckoutUI.showAddressForm()">Add Address</button>
            `;
            return;
        }
        
        container.innerHTML = `
            ${addresses.map((address, index) => `
                <div style="margin-bottom: 1rem; padding: 1rem; background: #f9fafb; border-radius: 8px;">
                    <label style="display: flex; align-items: center; gap: 1rem; cursor: pointer;">
                        <input type="radio" name="address" value="${address.id}" ${index === 0 ? 'checked' : ''}>
                        <div>
                            <div style="font-weight: 600;">${address.street}, ${address.city}, ${address.state} ${address.postalCode}, ${address.country}</div>
                            <div style="font-size: 0.875rem; color: #6b7280;">${address.defaultAddress ? 'Default Address' : address.addressType} - ${new Date(address.createdAt).toLocaleDateString()}</div>
                        </div>
                    </label>
                </div>
            `).join('')}
            
            <button class="btn btn-secondary" onclick="CheckoutUI.showAddressForm()" style="margin-top: 1rem;">
                + Add New Address
            </button>
        `;
    },
    
    // Render address form
    renderAddressForm() {
        const container = document.getElementById('address-form');
        if (!container) return;
        
        container.innerHTML = `
            <div style="background: #f9fafb; padding: 1rem; border-radius: 8px;">
                <h4 style="margin-bottom: 1rem;">Add New Address</h4>
                <div class="form-group">
                    <label class="form-label">Address Type</label>
                    <select class="form-control" id="addressType">
                        <option value="HOME">Home</option>
                        <option value="BILLING">Billing</option>
                        <option value="SHIPPING">Shipping</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Street</label>
                    <input type="text" class="form-control" id="street" placeholder="Street address">
                </div>
                <div class="form-group">
                    <label class="form-label">City</label>
                    <input type="text" class="form-control" id="city" placeholder="City">
                </div>
                <div class="form-group">
                    <label class="form-label">State</label>
                    <input type="text" class="form-control" id="state" placeholder="State">
                </div>
                <div class="form-group">
                    <label class="form-label">Postal Code</label>
                    <input type="text" class="form-control" id="postalCode" placeholder="Postal code">
                </div>
                <div class="form-group">
                    <label class="form-label">Country</label>
                    <input type="text" class="form-control" id="country" placeholder="Country">
                </div>
                <button class="btn btn-primary btn-block" onclick="CheckoutUI.saveAddress()">Save Address</button>
            </div>
        `;
    },
    
    // Save address
    async saveAddress() {
        const addressData = {
            addressType: document.getElementById('addressType').value,
            street: document.getElementById('street').value.trim(),
            city: document.getElementById('city').value.trim(),
            state: document.getElementById('state').value.trim(),
            postalCode: document.getElementById('postalCode').value.trim(),
            country: document.getElementById('country').value.trim()
        };
        
        if (!addressData.street || !addressData.city || !addressData.state || !addressData.postalCode || !addressData.country) {
            this.showError('Please fill in all fields');
            return;
        }
        
        try {
            const response = await SmartMartAPI.Address.create(addressData);
            this.addresses.push(response.data);
            this.renderAddressSelector(this.addresses);
            this.showSuccess('Address added successfully');
            this.showAddressForm(); // Toggle form visibility
        } catch (error) {
            this.showError(error.message);
        }
    },
    
    // Show address form
    showAddressForm() {
        const container = document.getElementById('address-form');
        if (container) {
            container.style.display = container.style.display === 'none' ? 'block' : 'none';
            if (container.style.display === 'block') {
                this.renderAddressForm();
            }
        }
    },
    
    // Show error message
    showError(message) {
        const container = document.getElementById('error-message');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-error">
                    <span class="alert-icon">⚠️</span>
                    <span>${message}</span>
                </div>
            `;
        }
    },
    
    // Show success message
    showSuccess(message) {
        const container = document.getElementById('success-message');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-success">
                    <span class="alert-icon">✅</span>
                    <span>${message}</span>
                </div>
            `;
        }
    },
    
    // Place order
    async placeOrder() {
        const paymentMethod = document.getElementById('payment-method').value;
        const addressId = document.querySelector('input[name="address"]:checked')?.value;
        
        if (!addressId) {
            this.showError('Please select or add an address');
            return;
        }
        
        // Get address details
        const address = this.addresses.find(a => a.id == addressId);
        
        const orderData = {
            paymentMethod: paymentMethod,
            shippingAddress: `${address.street}, ${address.city}, ${address.state} ${address.postalCode}, ${address.country}`,
            notes: document.getElementById('order-notes')?.value.trim() || ''
        };
        
        const btn = document.getElementById('place-order-btn');
        const originalText = btn.textContent;
        btn.textContent = 'Processing...';
        btn.disabled = true;
        
        try {
            const response = await SmartMartAPI.Order.placeOrder(
                orderData.paymentMethod,
                orderData.shippingAddress,
                orderData.notes
            );
            
            // Clear cart
            await SmartMartAPI.Cart.clearCart();
            
            // Show success
            this.showSuccess('Order placed successfully!');
            
            setTimeout(() => {
                window.location.href = 'orders.html';
            }, 2000);
            
        } catch (error) {
            this.showError(error.message || 'Failed to place order. Please try again.');
        } finally {
            btn.textContent = originalText;
            btn.disabled = false;
        }
    },
    
    // Bind events
    bindEvents() {
        const placeOrderBtn = document.getElementById('place-order-btn');
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener('click', () => this.placeOrder());
        }
    },
    
    // Initialize
    async initCheckout() {
        await this.init();
    }
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.CheckoutUI = CheckoutUI;
});