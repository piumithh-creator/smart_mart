package com.example.smartmart.controller;

import com.example.smartmart.dto.request.InventoryAdjustmentRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.InventoryResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Inventory", description = "Inventory management endpoints (ADMIN)")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for a product")
    public ResponseEntity<CommonResponse> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), inventoryService.findByProductId(productId), "Inventory retrieved"));
    }

    @PatchMapping("/product/{productId}/adjust")
    @Operation(summary = "Adjust inventory for a product")
    public ResponseEntity<CommonResponse> adjust(@PathVariable Long productId,
                                                                   @Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryResponse response = inventoryService.adjust(productId, request);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Inventory adjusted successfully"));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all low-stock items")
    public ResponseEntity<CommonResponse> getLowStock() {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), inventoryService.findLowStockItems(), "Low stock items retrieved"));
    }

    @GetMapping("/product/{productId}/transactions")
    @Operation(summary = "Get inventory transaction history for a product")
    public ResponseEntity<CommonResponse> getTransactions(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<?> response = inventoryService.getTransactionHistory(productId, PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Transactions retrieved"));
    }
}
