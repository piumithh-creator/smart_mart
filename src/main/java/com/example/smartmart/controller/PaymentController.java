package com.example.smartmart.controller;

import com.example.smartmart.dto.request.PaymentUpdateRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.PaymentResponse;
import com.example.smartmart.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<CommonResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), paymentService.getByOrderId(orderId), "Payment retrieved"));
    }

    @PatchMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update payment status (ADMIN)")
    public ResponseEntity<CommonResponse> update(@PathVariable Long orderId,
                                                                 @Valid @RequestBody PaymentUpdateRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), paymentService.updatePayment(orderId, request), "Payment updated"));
    }
}
