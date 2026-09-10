package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.PaymentUpdateRequest;
import com.example.smartmart.dto.response.PaymentResponse;
import com.example.smartmart.entity.Payment;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.PaymentRepository;
import com.example.smartmart.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(Long orderId, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));

        payment.setPaymentStatus(request.getPaymentStatus());
        if (request.getTransactionReference() != null) {
            payment.setTransactionReference(request.getTransactionReference());
        }
        Payment saved = paymentRepository.save(payment);
        logger.info("Payment updated for order ID: {} to status: {}", orderId, request.getPaymentStatus());
        return mapToResponse(saved);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
