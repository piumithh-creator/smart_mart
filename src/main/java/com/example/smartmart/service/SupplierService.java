package com.example.smartmart.service;

import com.example.smartmart.dto.request.SupplierRequest;
import com.example.smartmart.dto.response.SupplierResponse;
import java.util.List;

public interface SupplierService {
    SupplierResponse create(SupplierRequest request);
    List<SupplierResponse> findAll();
    SupplierResponse findById(Long id);
    SupplierResponse update(Long id, SupplierRequest request);
    void delete(Long id);
}
