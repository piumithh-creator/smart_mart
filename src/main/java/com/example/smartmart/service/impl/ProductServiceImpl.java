package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.ProductRequest;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ProductResponse;
import com.example.smartmart.entity.*;
import com.example.smartmart.enumiration.TransactionType;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.*;
import com.example.smartmart.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ReviewRepository reviewRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               SupplierRepository supplierRepository,
                               InventoryRepository inventoryRepository,
                               InventoryTransactionRepository transactionRepository,
                               ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
            product.setSupplier(supplier);
        }

        Product saved = productRepository.save(product);

        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 0;
        int threshold = request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10;

        Inventory inventory = Inventory.builder()
                .product(saved)
                .quantityInStock(initialStock)
                .lowStockThreshold(threshold)
                .build();
        Inventory savedInventory = inventoryRepository.save(inventory);

        if (initialStock > 0) {
            InventoryTransaction txn = InventoryTransaction.builder()
                    .inventory(savedInventory)
                    .transactionType(TransactionType.STOCK_IN)
                    .quantityChanged(initialStock)
                    .quantityAfter(initialStock)
                    .notes("Initial stock on product creation")
                    .build();
            transactionRepository.save(txn);
        }

        logger.info("Product created: {} (SKU: {})", saved.getName(), saved.getSku());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> findAll(Pageable pageable) {
        Page<ProductResponse> page = productRepository.findAll(pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return mapToResponse(findProductById(id));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProductById(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getActive() != null) product.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
            product.setSupplier(supplier);
        }

        logger.info("Product updated: {} (SKU: {})", product.getName(), product.getSku());
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse patch(Long id, ProductRequest request) {
        Product product = findProductById(id);

        if (request.getName() != null && !request.getName().isBlank())
            product.setName(request.getName());

        if (request.getSku() != null && !request.getSku().isBlank()) {
            if (!product.getSku().equalsIgnoreCase(request.getSku())
                    && productRepository.existsBySku(request.getSku())) {
                throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
            }
            product.setSku(request.getSku());
        }

        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getActive() != null) product.setActive(request.getActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
            product.setSupplier(supplier);
        }

        logger.info("Product patched: {}", product.getName());
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
        logger.info("Product deleted: {}", product.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> search(String keyword, Pageable pageable) {
        Page<ProductResponse> page = productRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> filter(Long categoryId, BigDecimal minPrice,
                                                  BigDecimal maxPrice, Pageable pageable) {
        Page<ProductResponse> page = productRepository.findWithFilters(categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private ProductResponse mapToResponse(Product product) {
        Integer stock = null;
        if (product.getInventory() != null) {
            stock = product.getInventory().getQuantityInStock();
        } else {
            stock = inventoryRepository.findByProductId(product.getId())
                    .map(Inventory::getQuantityInStock)
                    .orElse(0);
        }

        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setActive(product.isActive());
        response.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        response.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        response.setSupplierName(product.getSupplier() != null ? product.getSupplier().getName() : null);
        response.setSupplierId(product.getSupplier() != null ? product.getSupplier().getId() : null);
        response.setQuantityInStock(stock);
        response.setAverageRating(avgRating);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
