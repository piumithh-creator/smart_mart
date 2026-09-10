package com.example.smartmart.service;

import com.example.smartmart.dto.request.ReviewRequest;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse create(String email, ReviewRequest request);
    PagedResponse<ReviewResponse> getByProduct(Long productId, Pageable pageable);
    ReviewResponse update(String email, Long reviewId, ReviewRequest request);
    void delete(String email, Long reviewId);
}
