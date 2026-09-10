package com.example.smartmart.service;

import com.example.smartmart.dto.request.ProductRequest;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    PagedResponse<ProductResponse> findAll(Pageable pageable);
    ProductResponse findById(Long id);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse patch(Long id, ProductRequest request);
    void delete(Long id);
    PagedResponse<ProductResponse> search(String keyword, Pageable pageable);
    PagedResponse<ProductResponse> filter(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
