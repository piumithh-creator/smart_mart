package com.example.smartmart.dto.response;

import com.example.smartmart.enumiration.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionResponse {
    private Long id;
    private Long productId;
    private String productName;
    private TransactionType transactionType;
    private int quantityChanged;
    private int quantityAfter;
    private String notes;
    private LocalDateTime createdAt;
}
