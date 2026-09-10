package com.example.smartmart.controller;

import com.example.smartmart.dto.request.CustomerUpdateRequest;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.CustomerResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.service.CustomerService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Get all customers (ADMIN)")
    public ResponseEntity<CommonResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CustomerResponse> response = customerService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Customers retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID (ADMIN)")
    public ResponseEntity<CommonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), customerService.findById(id), "Customer retrieved"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile (USER)")
    public ResponseEntity<CommonResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), customerService.findByEmail(userDetails.getUsername()), "Profile retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer (ADMIN)")
    public ResponseEntity<CommonResponse> update(@PathVariable Long id,
                                                                  @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), customerService.update(id, request), "Customer updated"));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update own profile (USER)")
    public ResponseEntity<CommonResponse> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse me = customerService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), customerService.patch(me.getId(), request), "Profile updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer (ADMIN)")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Customer deleted"));
    }
}
