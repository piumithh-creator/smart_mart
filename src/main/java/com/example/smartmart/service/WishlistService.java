package com.example.smartmart.service;

import com.example.smartmart.dto.response.WishlistResponse;

public interface WishlistService {
    WishlistResponse getMyWishlist(String email);
    WishlistResponse addItem(String email, Long productId);
    WishlistResponse removeItem(String email, Long productId);
}
