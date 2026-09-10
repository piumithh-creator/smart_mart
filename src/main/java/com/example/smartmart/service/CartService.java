package com.example.smartmart.service;

import com.example.smartmart.dto.request.CartItemRequest;
import com.example.smartmart.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email);
    CartResponse addItem(String email, CartItemRequest request);
    CartResponse updateItem(String email, Long cartItemId, CartItemRequest request);
    CartResponse removeItem(String email, Long cartItemId);
    CartResponse clearCart(String email);
}
