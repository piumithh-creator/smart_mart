package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.AddressRequest;
import com.example.smartmart.dto.response.AddressResponse;
import com.example.smartmart.entity.Address;
import com.example.smartmart.entity.Customer;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.AddressRepository;
import com.example.smartmart.repository.CustomerRepository;
import com.example.smartmart.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    public AddressServiceImpl(AddressRepository addressRepository,
                               CustomerRepository customerRepository) {
        this.addressRepository = addressRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public AddressResponse create(String email, AddressRequest request) {
        Customer customer = getCustomer(email);
        Address address = Address.builder()
                .customer(customer)
                .addressType(request.getAddressType())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .defaultAddress(request.getDefaultAddress() != null ? request.getDefaultAddress() : false)
                .build();
        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String email) {
        Customer customer = getCustomer(email);
        return addressRepository.findByCustomerId(customer.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(String email, Long addressId) {
        Customer customer = getCustomer(email);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return mapToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse update(String email, Long addressId, AddressRequest request) {
        Customer customer = getCustomer(email);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        address.setAddressType(request.getAddressType());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        if (request.getDefaultAddress() != null) address.setDefaultAddress(request.getDefaultAddress());
        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse patch(String email, Long addressId, AddressRequest request) {
        Customer customer = getCustomer(email);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (request.getAddressType() != null) address.setAddressType(request.getAddressType());
        if (request.getStreet() != null && !request.getStreet().isBlank()) address.setStreet(request.getStreet());
        if (request.getCity() != null && !request.getCity().isBlank()) address.setCity(request.getCity());
        if (request.getState() != null && !request.getState().isBlank()) address.setState(request.getState());
        if (request.getPostalCode() != null && !request.getPostalCode().isBlank()) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null && !request.getCountry().isBlank()) address.setCountry(request.getCountry());
        if (request.getDefaultAddress() != null) address.setDefaultAddress(request.getDefaultAddress());
        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(String email, Long addressId) {
        Customer customer = getCustomer(email);
        Address address = addressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    private Customer getCustomer(String email) {
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .defaultAddress(address.isDefaultAddress())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
