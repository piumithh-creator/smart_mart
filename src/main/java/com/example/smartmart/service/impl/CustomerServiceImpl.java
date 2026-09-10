package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.CustomerUpdateRequest;
import com.example.smartmart.dto.response.CustomerResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.entity.Customer;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.CustomerRepository;
import com.example.smartmart.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> findAll(Pageable pageable) {
        Page<CustomerResponse> page = customerRepository.findAll(pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return mapToResponse(findCustomerById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByEmail(String email) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = findCustomerById(id);
        applyUpdate(customer, request);
        if (request.getFirstName() != null) customer.getUser().setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.getUser().setLastName(request.getLastName());
        logger.info("Customer updated: {}", customer.getUser().getEmail());
        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse patch(Long id, CustomerUpdateRequest request) {
        Customer customer = findCustomerById(id);
        applyUpdate(customer, request);
        if (request.getFirstName() != null) customer.getUser().setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.getUser().setLastName(request.getLastName());
        logger.info("Customer patched: {}", customer.getUser().getEmail());
        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomerById(id);
        customerRepository.delete(customer);
        logger.info("Customer deleted: {}", customer.getUser().getEmail());
    }

    private void applyUpdate(Customer customer, CustomerUpdateRequest request) {
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getDateOfBirth() != null) customer.setDateOfBirth(request.getDateOfBirth());
        if (request.getProfileImageUrl() != null) customer.setProfileImageUrl(request.getProfileImageUrl());
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    public CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUser().getId())
                .email(customer.getUser().getEmail())
                .firstName(customer.getUser().getFirstName())
                .lastName(customer.getUser().getLastName())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .profileImageUrl(customer.getProfileImageUrl())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
