package com.example.smartmart.controller;

import com.example.smartmart.dto.request.CartItemRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.CartResponse;
import com.example.smartmart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart management (USER)")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get my cart")
    public ResponseEntity<CommonResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), cartService.getCart(userDetails.getUsername()), "Cart retrieved"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CommonResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Item added to cart"));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CommonResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), cartService.updateItem(userDetails.getUsername(), cartItemId, request), "Cart item updated"));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CommonResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), cartService.removeItem(userDetails.getUsername(), cartItemId), "Item removed from cart"));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<CommonResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), cartService.clearCart(userDetails.getUsername()), "Cart cleared"));
    }
}
