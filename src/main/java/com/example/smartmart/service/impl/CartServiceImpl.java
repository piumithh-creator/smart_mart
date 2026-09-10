package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.CartItemRequest;
import com.example.smartmart.dto.response.CartItemResponse;
import com.example.smartmart.dto.response.CartResponse;
import com.example.smartmart.entity.*;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.*;
import com.example.smartmart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CartServiceImpl(CartRepository cartRepository,
                            CartItemRepository cartItemRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        Product product = findActiveProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        existingItem -> {
                            int newQty = existingItem.getQuantity() + request.getQuantity();
                            validateStock(product, newQty);
                            existingItem.setQuantity(newQty);
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            CartItem item = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(item);
                        }
                );

        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        logger.info("Item added to cart for user: {}", email);
        return mapToResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(String email, Long cartItemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .filter(ci -> ci.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String email, Long cartItemId) {
        Cart cart = getOrCreateCart(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .filter(ci -> ci.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        cartItemRepository.delete(item);
        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getCartItems().clear();
        Cart saved = cartRepository.save(cart);
        logger.info("Cart cleared for user: {}", email);
        return mapToResponse(saved);
    }

    private Cart getOrCreateCart(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().customer(customer).build();
                    return cartRepository.save(newCart);
                });
    }

    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (!product.isActive()) {
            throw new BusinessException("Product '" + product.getName() + "' is not available");
        }
        return product;
    }

    private void validateStock(Product product, int requestedQuantity) {
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new BusinessException("No inventory found for product: " + product.getName()));
        if (inventory.getQuantityInStock() < requestedQuantity) {
            throw new BusinessException("Insufficient stock for product: " + product.getName()
                    + ". Available: " + inventory.getQuantityInStock());
        }
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer().getId())
                .cartItems(items)
                .totalAmount(total)
                .totalItems(items.size())
                .build();
    }
}
