package com.example.smartmart.controller;

import com.example.smartmart.dto.request.ReviewRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ReviewResponse;
import com.example.smartmart.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Product review endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a product review (USER)")
    public ResponseEntity<CommonResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.create(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Review submitted successfully"));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews for a product (PUBLIC)")
    public ResponseEntity<CommonResponse> getByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ReviewResponse> response = reviewService.getByProduct(productId, PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Reviews retrieved"));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update own review (USER)")
    public ResponseEntity<CommonResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), reviewService.update(userDetails.getUsername(), id, request), "Review updated"));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete own review (USER)")
    public ResponseEntity<CommonResponse> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        reviewService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Review deleted"));
    }
}
