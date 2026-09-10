package com.example.smartmart.dto.response;

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
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private int quantityInStock;
    private int lowStockThreshold;
    private boolean lowStock;
    private LocalDateTime updatedAt;
}
