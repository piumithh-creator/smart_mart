package com.example.smartmart.service;

import com.example.smartmart.dto.request.AddressRequest;
import com.example.smartmart.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
    AddressResponse create(String email, AddressRequest request);
    List<AddressResponse> getMyAddresses(String email);
    AddressResponse getById(String email, Long addressId);
    AddressResponse update(String email, Long addressId, AddressRequest request);
    AddressResponse patch(String email, Long addressId, AddressRequest request);
    void delete(String email, Long addressId);
}
