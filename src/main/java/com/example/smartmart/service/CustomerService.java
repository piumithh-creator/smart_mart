package com.example.smartmart.service;

import com.example.smartmart.dto.request.CustomerUpdateRequest;
import com.example.smartmart.dto.response.CustomerResponse;
import com.example.smartmart.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    PagedResponse<CustomerResponse> findAll(Pageable pageable);
    CustomerResponse findById(Long id);
    CustomerResponse findByEmail(String email);
    CustomerResponse update(Long id, CustomerUpdateRequest request);
    CustomerResponse patch(Long id, CustomerUpdateRequest request);
    void delete(Long id);
}
