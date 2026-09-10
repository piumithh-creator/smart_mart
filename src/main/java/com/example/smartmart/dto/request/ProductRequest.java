package com.example.smartmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private Boolean active = true;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @PositiveOrZero(message = "Initial stock cannot be negative")
    private Integer initialStock = 0;

    @PositiveOrZero(message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;
}
