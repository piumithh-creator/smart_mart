package com.example.smartmart.controller;

import com.example.smartmart.dto.request.SupplierRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.SupplierResponse;
import com.example.smartmart.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Suppliers", description = "Supplier management endpoints (ADMIN)")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Supplier created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<CommonResponse> findAll() {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), supplierService.findAll(), "Suppliers retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<CommonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), supplierService.findById(id), "Supplier retrieved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier")
    public ResponseEntity<CommonResponse> update(@PathVariable Long id,
                                                                  @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), supplierService.update(id, request), "Supplier updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Supplier deleted successfully"));
    }
}
