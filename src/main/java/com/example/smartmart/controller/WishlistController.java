package com.example.smartmart.controller;

import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.WishlistResponse;
import com.example.smartmart.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Wishlist management (USER)")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    @Operation(summary = "Get my wishlist")
    public ResponseEntity<CommonResponse> getMyWishlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        WishlistResponse response = wishlistService.getMyWishlist(userDetails.getUsername());
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Wishlist retrieved"));
    }

    @PostMapping("/items/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<CommonResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        WishlistResponse response = wishlistService.addItem(userDetails.getUsername(), productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Product added to wishlist"));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<CommonResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        WishlistResponse response = wishlistService.removeItem(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Product removed from wishlist"));
    }
}
