package com.example.smartmart.dto.request;

import com.example.smartmart.enumiration.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentUpdateRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
    private String transactionReference;
}
