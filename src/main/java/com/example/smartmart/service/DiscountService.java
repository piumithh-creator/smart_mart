package com.example.smartmart.service;

import com.example.smartmart.dto.request.DiscountRequest;
import com.example.smartmart.dto.response.DiscountResponse;
import java.util.List;

public interface DiscountService {
    DiscountResponse create(DiscountRequest request);
    List<DiscountResponse> findAll();
    DiscountResponse findById(Long id);
    DiscountResponse update(Long id, DiscountRequest request);
    DiscountResponse toggleActive(Long id);
    void assignToProduct(Long discountId, Long productId);
    void removeFromProduct(Long discountId, Long productId);
    void delete(Long id);
}
