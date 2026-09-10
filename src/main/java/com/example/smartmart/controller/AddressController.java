package com.example.smartmart.controller;

import com.example.smartmart.dto.request.AddressRequest;
import com.example.smartmart.dto.response.AddressResponse;
import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Addresses", description = "Customer address management (USER)")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @Operation(summary = "Add a new address")
    public ResponseEntity<CommonResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.create(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CommonResponse(HttpStatus.CREATED.value(), response, "Address created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get my addresses")
    public ResponseEntity<CommonResponse> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AddressResponse> response = addressService.getMyAddresses(userDetails.getUsername());
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Addresses retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<CommonResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AddressResponse response = addressService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Address retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an address")
    public ResponseEntity<CommonResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Address updated"));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an address")
    public ResponseEntity<CommonResponse> patch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody AddressRequest request) {
        AddressResponse response = addressService.patch(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Address updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<CommonResponse> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        addressService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "Address deleted"));
    }
}
