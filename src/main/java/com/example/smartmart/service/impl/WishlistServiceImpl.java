package com.example.smartmart.service.impl;

import com.example.smartmart.dto.response.WishlistItemResponse;
import com.example.smartmart.dto.response.WishlistResponse;
import com.example.smartmart.entity.*;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.*;
import com.example.smartmart.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                                WishlistItemRepository wishlistItemRepository,
                                CustomerRepository customerRepository,
                                ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getMyWishlist(String email) {
        Wishlist wishlist = getOrCreateWishlist(email);
        return mapToResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse addItem(String email, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new DuplicateResourceException("Product is already in your wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
        wishlistItemRepository.save(item);

        Wishlist updated = wishlistRepository.findById(wishlist.getId()).orElse(wishlist);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public WishlistResponse removeItem(String email, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(email);
        WishlistItem item = wishlistItemRepository
                .findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        wishlistItemRepository.delete(item);

        Wishlist updated = wishlistRepository.findById(wishlist.getId()).orElse(wishlist);
        return mapToResponse(updated);
    }

    private Wishlist getOrCreateWishlist(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        return wishlistRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder().customer(customer).build();
                    return wishlistRepository.save(newWishlist);
                });
    }

    private WishlistResponse mapToResponse(Wishlist wishlist) {
        List<WishlistItemResponse> items = wishlist.getWishlistItems().stream()
                .map(item -> WishlistItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .productPrice(item.getProduct().getPrice())
                        .productActive(item.getProduct().isActive())
                        .addedAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .customerId(wishlist.getCustomer().getId())
                .wishlistItems(items)
                .totalItems(items.size())
                .build();
    }
}
