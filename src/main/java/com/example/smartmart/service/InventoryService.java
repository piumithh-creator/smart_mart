package com.example.smartmart.service;

import com.example.smartmart.dto.request.InventoryAdjustmentRequest;
import com.example.smartmart.dto.response.InventoryResponse;
import com.example.smartmart.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface InventoryService {
    InventoryResponse findByProductId(Long productId);
    InventoryResponse adjust(Long productId, InventoryAdjustmentRequest request);
    List<InventoryResponse> findLowStockItems();
    PagedResponse<?> getTransactionHistory(Long productId, Pageable pageable);
}
