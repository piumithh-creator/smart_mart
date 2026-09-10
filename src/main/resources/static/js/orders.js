// =========================================
// Orders Module
// =========================================

const OrdersUI = {
    orders: [],
    currentPage: 0,
    totalPages: 0,
    
    // Render orders list
    async renderOrders(page = 0) {
        const container = document.getElementById('orders-container');
        
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }
        
        if (container) container.innerHTML = '<div class="spinner"></div>';
        
        try {
            const response = await SmartMartAPI.Order.getMyOrders(page);
            this.orders = response.data.content;
            this.currentPage = response.data.pageNumber;
            this.totalPages = response.data.totalPages;
            
            if (this.orders.length === 0) {
                if (container) {
                    container.innerHTML = `
                        <div class="alert alert-warning">
                            <span class="alert-icon">📦</span>
                            <span>No orders yet. Start shopping!</span>
                        </div>
                    `;
                }
                return;
            }
            
            if (container) {
                container.innerHTML = `
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Order Number</th>
                                    <th>Date</th>
                                    <th>Items</th>
                                    <th>Total</th>
                                    <th>Status</th>
                                    <th>Payment</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${this.orders.map(order => `
                                    <tr>
                                        <td>${order.orderNumber}</td>
                                        <td>${new Date(order.createdAt).toLocaleDateString()}</td>
                                        <td>${order.orderItems.length} items</td>
                                        <td>$${order.finalAmount.toFixed(2)}</td>
                                        <td>
                                            <span class="badge badge-${this.getStatusClass(order.status)}">
                                                ${order.status}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge badge-${this.getPaymentClass(order.payment?.paymentStatus)}">
                                                ${order.payment?.paymentStatus || 'N/A'}
                                            </span>
                                        </td>
                                        <td>
                                            <button class="btn btn-primary btn-sm" onclick="OrdersUI.viewOrder(${order.id})">View</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                    
                    ${this.renderPagination()}
                `;
            }
            
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
    
    // Render pagination
    renderPagination() {
        if (this.totalPages <= 1) return '';
        
        let html = '<div class="pagination">';
        
        // Previous button
        html += `<button onclick="OrdersUI.renderOrders(${this.currentPage - 1})" ${this.currentPage === 0 ? 'disabled' : ''}>Previous</button>`;
        
        // Page numbers
        for (let i = 0; i < this.totalPages; i++) {
            html += `<button onclick="OrdersUI.renderOrders(${i})" ${this.currentPage === i ? 'class="active"' : ''}>${i + 1}</button>`;
        }
        
        // Next button
        html += `<button onclick="OrdersUI.renderOrders(${this.currentPage + 1})" ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}>Next</button>`;
        
        html += '</div>';
        
        return html;
    },
    
    // Get status CSS class
    getStatusClass(status) {
        switch(status) {
            case 'PENDING': return 'warning';
            case 'CONFIRMED': return 'warning';
            case 'PROCESSING': return 'warning';
            case 'SHIPPED': return 'warning';
            case 'DELIVERED': return 'success';
            case 'CANCELLED': return 'error';
            default: return '';
        }
    },
    
    // Get payment status CSS class
    getPaymentClass(status) {
        switch(status) {
            case 'PAID': return 'success';
            case 'PENDING': return 'warning';
            case 'FAILED': return 'error';
            case 'REFUNDED': return 'warning';
            default: return '';
        }
    },
    
    // View order details
    async viewOrder(orderId) {
        try {
            const response = await SmartMartAPI.Order.getById(orderId);
            const order = response.data;
            
            this.showOrderDetails(order);
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Show order details in modal
    showOrderDetails(order) {
        const modal = document.getElementById('order-modal');
        if (!modal) return;
        
        const itemsHtml = order.orderItems.map(item => `
            <div class="summary-row">
                <span>${item.productName} x ${item.quantity}</span>
                <span>$${item.subtotal.toFixed(2)}</span>
            </div>
        `).join('');
        
        modal.innerHTML = `
            <div class="modal-header">
                <h2>Order #${order.orderNumber}</h2>
                <button class="modal-close" onclick="OrdersUI.closeModal()">&times;</button>
            </div>
            <div class="modal-body">
                <div class="order-details">
                    <div class="summary-row">
                        <span>Date:</span>
                        <span>${new Date(order.createdAt).toLocaleString()}</span>
                    </div>
                    <div class="summary-row">
                        <span>Status:</span>
                        <span>${order.status}</span>
                    </div>
                    <div class="summary-row">
                        <span>Payment:</span>
                        <span>${order.payment?.paymentMethod || 'N/A'} - ${order.payment?.paymentStatus || 'N/A'}</span>
                    </div>
                    <div class="summary-row">
                        <span>Shipping Address:</span>
                        <span>${order.shippingAddress}</span>
                    </div>
                </div>
                
                <h3>Order Items</h3>
                ${itemsHtml}
                
                <div class="summary-row" style="margin-top: 20px; border-top: 2px solid #2563eb;">
                    <span>Order Total:</span>
                    <span>$${order.finalAmount.toFixed(2)}</span>
                </div>
                
                ${order.notes ? `<p><strong>Notes:</strong> ${order.notes}</p>` : ''}
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="OrdersUI.closeModal()">Close</button>
            </div>
        `;
        
        modal.classList.add('active');
    },
    
    // Close modal
    closeModal() {
        const modal = document.getElementById('order-modal');
        if (modal) {
            modal.classList.remove('active');
        }
    },
    
    // Initialize orders module
    init() {
        this.renderOrders();
        
        // Close modal on background click
        const modal = document.getElementById('order-modal');
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    this.closeModal();
                }
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
    window.OrdersUI = OrdersUI;
});