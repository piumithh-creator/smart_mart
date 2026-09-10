package com.example.smartmart.controller;

import com.example.smartmart.dto.request.DiscountRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.DiscountResponse;
import com.example.smartmart.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Discounts", description = "Discount management endpoints (ADMIN)")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    @Operation(summary = "Create a new discount")
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody DiscountRequest request) {
        DiscountResponse response = discountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Discount created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all discounts")
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), discountService.findAll(), "Discounts retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID")
    public ResponseEntity<CommonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), discountService.findById(id), "Discount retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a discount")
    public ResponseEntity<CommonResponse> update(@PathVariable Long id,
                                                                  @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), discountService.update(id, request), "Discount updated"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle discount active status")
    public ResponseEntity<CommonResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), discountService.toggleActive(id), "Discount status toggled"));
    }

    @PostMapping("/{discountId}/products/{productId}")
    @Operation(summary = "Assign discount to a product")
    public ResponseEntity<CommonResponse> assignToProduct(@PathVariable Long discountId,
                                                               @PathVariable Long productId) {
        discountService.assignToProduct(discountId, productId);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Discount assigned to product"));
    }

    @DeleteMapping("/{discountId}/products/{productId}")
    @Operation(summary = "Remove discount from a product")
    public ResponseEntity<CommonResponse> removeFromProduct(@PathVariable Long discountId,
                                                                  @PathVariable Long productId) {
        discountService.removeFromProduct(discountId, productId);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Discount removed from product"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a discount")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Discount deleted"));
    }
}
