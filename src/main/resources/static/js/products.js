// =========================================
// Products Module
// =========================================

const ProductsUI = {
    currentPage: 0,
    totalPages: 0,
    keyword: '',
    categoryId: null,
    
    // Load products
    async loadProducts(page = 0) {
        const container = document.getElementById('products-container');
        if (container) container.innerHTML = '<div class="spinner"></div>';
        
        try {
            const keyword = document.getElementById('search-input-filter').value.trim();
            const categoryId = document.getElementById('category-select').value;
            const sortBy = document.getElementById('sort-select').value;
            
            this.keyword = keyword;
            this.categoryId = categoryId;
            
            const response = await SmartMartAPI.Product.getAll(
                page, 
                12, 
                sortBy, 
                categoryId || null,
                null, 
                null
            );
            
            this.currentPage = response.data.pageNumber;
            this.totalPages = response.data.totalPages;
            
            const products = response.data.content || response.data;
            
            if (container) {
                if (products.length === 0) {
                    container.innerHTML = `
                        <div class="alert alert-warning" style="text-align: center;">
                            <span class="alert-icon">🔍</span>
                            <span>No products found matching your criteria.</span>
                        </div>
                    `;
                } else {
                    container.innerHTML = products.map(product => `
                        <div class="product-card">
                            <div class="product-card-image">
                                <img src="${this.getImagePlaceholder()}" alt="${product.name}">
                            </div>
                            <div class="product-card-body">
                                <div class="product-card-category">${product.categoryName || ''}</div>
                                <h3 class="product-card-title">${product.name}</h3>
                                <div class="product-card-price">$${product.price.toFixed(2)}</div>
                                <div class="product-card-stock ${this.getStockClass(product.quantityInStock)}">
                                    ${this.getStockText(product.quantityInStock)}
                                </div>
                                <div class="product-card-actions">
                                    <button class="btn btn-primary" onclick="ProductsUI.viewProduct(${product.id})">Details</button>
                                    <button class="btn btn-outline" onclick="ProductsUI.addToCart(${product.id})">
                                        <i class="fas fa-shopping-cart"></i> Add
                                    </button>
                                </div>
                            </div>
                        </div>
                    `).join('');
                    
                    this.renderPagination();
                }
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
        const container = document.getElementById('pagination');
        if (!container || this.totalPages <= 1) return;
        
        container.style.display = 'flex';
        
        let html = '';
        
        // Previous button
        html += `<button onclick="ProductsUI.loadProducts(${this.currentPage - 1})" ${this.currentPage === 0 ? 'disabled' : ''}>Previous</button>`;
        
        // Page numbers
        for (let i = 0; i < this.totalPages; i++) {
            html += `<button onclick="ProductsUI.loadProducts(${i})" ${this.currentPage === i ? 'class="active"' : ''}>${i + 1}</button>`;
        }
        
        // Next button
        html += `<button onclick="ProductsUI.loadProducts(${this.currentPage + 1})" ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}>Next</button>`;
        
        container.innerHTML = `<div class="pagination">${html}</div>`;
    },
    
    // View product details
    viewProduct(id) {
        window.location.href = `product-details.html?id=${id}`;
    },
    
    // Add to cart
    async addToCart(productId) {
        if (!SmartMartAPI.Auth.isAuthenticated()) {
            alert('Please login to add items to cart');
            window.location.href = 'login.html';
            return;
        }
        
        try {
            await SmartMartAPI.Cart.addItem(productId, 1);
            alert('Item added to cart!');
            AuthUI.renderNavbar();
        } catch (error) {
            alert(error.message);
        }
    },
    
    // Load categories for filter
    async loadCategories() {
        try {
            const response = await SmartMartAPI.Category.getAll();
            const categories = response.data;
            const select = document.getElementById('category-select');
            
            if (select && categories.length > 0) {
                select.innerHTML = '<option value="">All Categories</option>' + 
                    categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
            }
        } catch (error) {
            console.error('Failed to load categories:', error);
        }
    },
    
    // Get image placeholder
    getImagePlaceholder() {
        return 'https://via.placeholder.com/280x200/cccccc/666666?text=No+Image';
    },
    
    // Get stock class
    getStockClass(quantity) {
        if (quantity === 0) return 'out-of-stock';
        if (quantity < 10) return 'low-stock';
        return 'in-stock';
    },
    
    // Get stock text
    getStockText(quantity) {
        if (quantity === 0) return 'Out of Stock';
        if (quantity < 10) return 'Low Stock';
        return 'In Stock';
    },
    
    // Initialize products module
    init() {
        // Load products if on products page
        if (document.getElementById('products-container')) {
            const urlParams = new URLSearchParams(window.location.search);
            const search = urlParams.get('search');
            const category = urlParams.get('category');
            
            if (search) {
                document.getElementById('search-input-filter').value = search;
            }
            if (category) {
                document.getElementById('category-select').value = category;
            }
            
            this.loadProducts(0);
        }
    }
};

// Filter products
function filterProducts() {
    ProductsUI.loadProducts(0);
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.ProductsUI = ProductsUI;
    ProductsUI.init();
});