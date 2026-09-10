package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.CategoryRequest;
import com.example.smartmart.dto.response.CategoryResponse;
import com.example.smartmart.entity.Category;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.CategoryRepository;
import com.example.smartmart.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        Category saved = categoryRepository.save(category);
        logger.info("Category created: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return mapToResponse(findCategoryById(id));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);
        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getActive() != null) category.setActive(request.getActive());
        logger.info("Category updated: {}", category.getName());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse patch(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            if (!category.getName().equalsIgnoreCase(request.getName())
                    && categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
            }
            category.setName(request.getName());
        }
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getActive() != null) category.setActive(request.getActive());
        logger.info("Category patched: {}", category.getName());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
        logger.info("Category deleted: {}", category.getName());
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
