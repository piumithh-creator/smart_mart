package com.example.smartmart.controller;

import com.example.smartmart.dto.request.OrderRequest;
import com.example.smartmart.dto.request.OrderStatusUpdateRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.OrderResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place a new order (USER)")
    public ResponseEntity<CommonResponse> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Order placed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get my orders (USER)")
    public ResponseEntity<CommonResponse> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<OrderResponse> response = orderService.getMyOrders(userDetails.getUsername(),
                PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Orders retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<CommonResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), orderService.getOrderById(userDetails.getUsername(), id), "Order retrieved"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (ADMIN)")
    public ResponseEntity<CommonResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), orderService.updateOrderStatus(id, request), "Order status updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an order (USER)")
    public ResponseEntity<CommonResponse> cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        orderService.cancelOrder(userDetails.getUsername(), id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Order cancelled"));
    }
}
