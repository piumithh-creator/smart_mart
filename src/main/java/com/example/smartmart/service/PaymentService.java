package com.example.smartmart.service;

import com.example.smartmart.dto.request.PaymentUpdateRequest;
import com.example.smartmart.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse getByOrderId(Long orderId);
    PaymentResponse updatePayment(Long orderId, PaymentUpdateRequest request);
}
