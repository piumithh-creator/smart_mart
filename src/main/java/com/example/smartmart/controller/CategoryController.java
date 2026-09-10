package com.example.smartmart.controller;

import com.example.smartmart.dto.request.CategoryRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.CategoryResponse;
import com.example.smartmart.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new category (ADMIN)")
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Category created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all categories (PUBLIC)")
    public ResponseEntity<CommonResponse> findAll() {
        List<CategoryResponse> response = categoryService.findAll();
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Categories retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID (PUBLIC)")
    public ResponseEntity<CommonResponse> findById(@PathVariable Long id) {
        CategoryResponse response = categoryService.findById(id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Category retrieved successfully"));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a category (ADMIN)")
    public ResponseEntity<CommonResponse> update(@PathVariable Long id,
                                                                 @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Category updated successfully"));
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Partially update a category (ADMIN)")
    public ResponseEntity<CommonResponse> patch(@PathVariable Long id,
                                                                @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.patch(id, request);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a category (ADMIN)")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Category deleted successfully"));
    }
}
