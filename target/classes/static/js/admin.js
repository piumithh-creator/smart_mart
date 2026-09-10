// =========================================
// Admin Module
// =========================================

const AdminUI = {
    categories: [],
    suppliers: [],
    currentModalProduct: null,
    currentModalCategory: null,
    
    // Render dashboard
    async renderDashboard() {
        const container = document.getElementById('dashboard-container');
        
        try {
            const dashboard = await SmartMartAPI.Admin.getDashboard();
            const inventory = await SmartMartAPI.Admin.getInventorySummary();
            
            if (container) {
                container.innerHTML = `
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-icon">📦</div>
                            <div class="stat-info">
                                <h4>Total Products</h4>
                                <p>${dashboard.data.totalProducts}</p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon">👥</div>
                            <div class="stat-info">
                                <h4>Total Customers</h4>
                                <p>${dashboard.data.totalCustomers}</p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon">🛒</div>
                            <div class="stat-info">
                                <h4>Total Orders</h4>
                                <p>${dashboard.data.totalOrders}</p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon">💰</div>
                            <div class="stat-info">
                                <h4>Total Sales</h4>
                                <p>$${dashboard.data.totalSales.toFixed(2)}</p>
                            </div>
                        </div>
                    </div>
                    
                    <div class="stats-grid">
                        <div class="stat-card" style="border-left: 4px solid #f59e0b;">
                            <div class="stat-icon">⚠️</div>
                            <div class="stat-info">
                                <h4>Pending Orders</h4>
                                <p>${dashboard.data.pendingOrders}</p>
                            </div>
                        </div>
                        <div class="stat-card" style="border-left: 4px solid #10b981;">
                            <div class="stat-icon">✅</div>
                            <div class="stat-info">
                                <h4>Completed Orders</h4>
                                <p>${dashboard.data.completedOrders}</p>
                            </div>
                        </div>
                        <div class="stat-card" style="border-left: 4px solid #ef4444;">
                            <div class="stat-icon">❌</div>
                            <div class="stat-info">
                                <h4>Cancelled Orders</h4>
                                <p>${dashboard.data.cancelledOrders}</p>
                            </div>
                        </div>
                        <div class="stat-card" style="border-left: 4px solid #dc2626;">
                            <div class="stat-icon">📉</div>
                            <div class="stat-info">
                                <h4>Low Stock Products</h4>
                                <p>${dashboard.data.lowStockProducts}</p>
                            </div>
                        </div>
                    </div>
                    
                    ${this.renderLowStockTable(inventory.data)}
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
    
    // Render low stock table
    renderLowStockTable(items) {
        if (!items || items.length === 0) {
            return '<div class="alert alert-success"><span class="alert-icon">✅</span><span>All products are in stock!</span></div>';
        }
        
        return `
            <div class="table-container">
                <h3>Low Stock Products</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>SKU</th>
                            <th>Current Stock</th>
                            <th>Low Stock Threshold</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${items.map(item => `
                            <tr>
                                <td>${item.productName}</td>
                                <td>${item.productSku}</td>
                                <td>${item.quantityInStock}</td>
                                <td>${item.lowStockThreshold}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    },
    
    // Render admin sidebar navigation
    renderSidebar() {
        const container = document.getElementById('admin-sidebar');
        if (container) {
            container.innerHTML = `
                <div class="admin-sidebar-header">
                    <h2>Admin Panel</h2>
                </div>
                <nav class="admin-sidebar-nav">
                    <a href="dashboard.html" class="admin-nav-link">📊 Dashboard</a>
                    <a href="products.html" class="admin-nav-link">📦 Products</a>
                    <a href="categories.html" class="admin-nav-link">📁 Categories</a>
                    <a href="suppliers.html" class="admin-nav-link">🏪 Suppliers</a>
                    <a href="customers.html" class="admin-nav-link">👥 Customers</a>
                    <a href="orders.html" class="admin-nav-link">📦 Orders</a>
                    <a href="inventory.html" class="admin-nav-link">📊 Inventory</a>
                </nav>
            `;
        }
    },
    
    // Toggle sidebar
    toggleSidebar() {
        const sidebar = document.querySelector('.admin-sidebar');
        if (sidebar) {
            sidebar.classList.toggle('collapsed');
        }
    },
    
    // Load categories
    async loadCategories() {
        try {
            const response = await SmartMartAPI.Category.getAll();
            this.categories = response.data;
            
            // Update category selects
            this.updateCategorySelects();
        } catch (error) {
            console.error('Failed to load categories:', error);
        }
    },
    
    // Load suppliers
    async loadSuppliers() {
        try {
            const response = await SmartMartAPI.Supplier.getAll();
            this.suppliers = response.data;
            
            // Update supplier selects
            this.updateSupplierSelects();
        } catch (error) {
            console.error('Failed to load suppliers:', error);
        }
    },
    
    // Update category selects
    updateCategorySelects() {
        const productSelect = document.getElementById('product-category');
        if (productSelect) {
            productSelect.innerHTML = '<option value="">Select Category</option>' +
                this.categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
        }
    },
    
    // Update supplier selects
    updateSupplierSelects() {
        const productSelect = document.getElementById('product-supplier');
        if (productSelect) {
            productSelect.innerHTML = '<option value="">Select Supplier</option>' +
                this.suppliers.map(sup => `<option value="${sup.id}">${sup.name}</option>`).join('');
        }
    },
    
    // Show add product form
    showAddProductForm() {
        document.getElementById('product-modal-title').textContent = 'Add Product';
        document.getElementById('product-id').value = '';
        document.getElementById('product-name').value = '';
        document.getElementById('product-sku').value = '';
        document.getElementById('product-description').value = '';
        document.getElementById('product-price').value = '';
        document.getElementById('product-stock').value = '10';
        document.getElementById('product-active').value = 'true';
        
        const modal = document.getElementById('product-modal');
        if (modal) {
            modal.classList.add('active');
        }
    },
    
    // Close product modal
    closeProductModal() {
        const modal = document.getElementById('product-modal');
        if (modal) {
            modal.classList.remove('active');
        }
    },
    
    // Edit product
    async editProduct(id) {
        try {
            const response = await SmartMartAPI.Product.getById(id);
            const product = response.data;
            
            document.getElementById('product-modal-title').textContent = 'Edit Product';
            document.getElementById('product-id').value = product.id;
            document.getElementById('product-name').value = product.name;
            document.getElementById('product-sku').value = product.sku;
            document.getElementById('product-description').value = product.description || '';
            document.getElementById('product-price').value = product.price;
            document.getElementById('product-category').value = product.categoryId || '';
            document.getElementById('product-supplier').value = product.supplierId || '';
            document.getElementById('product-stock').value = product.quantityInStock || '0';
            document.getElementById('product-active').value = product.active ? 'true' : 'false';
            
            const modal = document.getElementById('product-modal');
            if (modal) {
                modal.classList.add('active');
            }
            
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Save product
    async saveProduct() {
        const productId = document.getElementById('product-id').value;
        const productData = {
            name: document.getElementById('product-name').value.trim(),
            sku: document.getElementById('product-sku').value.trim(),
            description: document.getElementById('product-description').value.trim(),
            price: parseFloat(document.getElementById('product-price').value),
            categoryId: parseInt(document.getElementById('product-category').value) || null,
            supplierId: parseInt(document.getElementById('product-supplier').value) || null,
            initialStock: parseInt(document.getElementById('product-stock').value) || 0,
            active: document.getElementById('product-active').value === 'true'
        };
        
        if (!productData.name || !productData.sku || !productData.price) {
            alert('Please fill in all required fields');
            return;
        }
        
        try {
            if (productId) {
                await SmartMartAPI.Product.update(productId, productData);
                alert('Product updated successfully');
            } else {
                await SmartMartAPI.Product.create(productData);
                alert('Product created successfully');
            }
            
            this.closeProductModal();
            await this.renderProductList();
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Delete product
    async deleteProduct(id) {
        if (!confirm('Are you sure you want to delete this product?')) return;
        
        try {
            await SmartMartAPI.Product.delete(id);
            alert('Product deleted successfully');
            await this.renderProductList();
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Render product list for admin
    async renderProductList() {
        const container = document.getElementById('product-list');
        if (container) container.innerHTML = '<div class="spinner"></div>';
        
        try {
            const response = await SmartMartAPI.Product.getAll(0, 50);
            const products = response.data.content || response.data;
            
            if (container) {
                container.innerHTML = `
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>SKU</th>
                                    <th>Price</th>
                                    <th>Stock</th>
                                    <th>Category</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${products.map(product => `
                                    <tr>
                                        <td>${product.name}</td>
                                        <td>${product.sku}</td>
                                        <td>$${product.price.toFixed(2)}</td>
                                        <td>${product.quantityInStock}</td>
                                        <td>${product.categoryName || '-'}</td>
                                        <td>
                                            <span class="badge badge-${product.active ? 'success' : 'error'}">
                                                ${product.active ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td>
                                            <button class="btn btn-primary btn-sm" onclick="AdminUI.editProduct(${product.id})">Edit</button>
                                            <button class="btn btn-danger btn-sm" onclick="AdminUI.deleteProduct(${product.id})">Delete</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
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
    
    // Render category list for admin
    async renderCategoryList() {
        const container = document.getElementById('category-list');
        if (container) container.innerHTML = '<div class="spinner"></div>';
        
        try {
            const response = await SmartMartAPI.Category.getAll();
            const categories = response.data;
            
            if (container) {
                container.innerHTML = `
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Description</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${categories.map(category => `
                                    <tr>
                                        <td>${category.name}</td>
                                        <td>${category.description || '-'}</td>
                                        <td>
                                            <span class="badge badge-${category.active ? 'success' : 'error'}">
                                                ${category.active ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td>
                                            <button class="btn btn-primary btn-sm" onclick="AdminUI.editCategory(${category.id})">Edit</button>
                                            <button class="btn btn-danger btn-sm" onclick="AdminUI.deleteCategory(${category.id})">Delete</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
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
    
    // Edit category
    async editCategory(id) {
        try {
            const response = await SmartMartAPI.Category.getById(id);
            const category = response.data;
            
            alert('Edit category: ' + category.name);
            // Add edit functionality as needed
            
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Delete category
    async deleteCategory(id) {
        if (!confirm('Are you sure you want to delete this category?')) return;
        
        try {
            await SmartMartAPI.Category.delete(id);
            alert('Category deleted successfully');
            await this.renderCategoryList();
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Initialize admin module
    init() {
        this.renderSidebar();
        this.checkAdmin();
        
        // Navigation clicks
        document.querySelectorAll('.admin-nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                document.querySelectorAll('.admin-nav-link').forEach(l => l.classList.remove('active'));
                e.target.classList.add('active');
            });
        });
    },
    
    // Check admin access
    checkAdmin() {
        if (!SmartMartAPI.Auth.isAdmin()) {
            window.location.href = '../index.html';
        }
    }
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.AdminUI = AdminUI;
});