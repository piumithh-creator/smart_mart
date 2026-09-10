package com.example.smartmart.service;

import com.example.smartmart.dto.request.CategoryRequest;
import com.example.smartmart.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    List<CategoryResponse> findAll();
    CategoryResponse findById(Long id);
    CategoryResponse update(Long id, CategoryRequest request);
    CategoryResponse patch(Long id, CategoryRequest request);
    void delete(Long id);
}
