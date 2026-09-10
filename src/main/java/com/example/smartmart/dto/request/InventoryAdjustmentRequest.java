package com.example.smartmart.dto.request;

import com.example.smartmart.enumiration.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryAdjustmentRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 255, message = "Notes must not exceed 255 characters")
    private String notes;
}
