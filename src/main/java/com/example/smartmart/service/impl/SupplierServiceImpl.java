package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.SupplierRequest;
import com.example.smartmart.dto.response.SupplierResponse;
import com.example.smartmart.entity.Supplier;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.SupplierRepository;
import com.example.smartmart.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (request.getEmail() != null && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Supplier with email '" + request.getEmail() + "' already exists");
        }
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .contactPerson(request.getContactPerson())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        Supplier saved = supplierRepository.save(supplier);
        logger.info("Supplier created: {}", saved.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return mapToResponse(findSupplierById(id));
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findSupplierById(id);
        if (request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(supplier.getEmail())
                && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Supplier with email '" + request.getEmail() + "' already exists");
        }
        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setContactPerson(request.getContactPerson());
        if (request.getActive() != null) supplier.setActive(request.getActive());
        logger.info("Supplier updated: {}", supplier.getName());
        return mapToResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Supplier supplier = findSupplierById(id);
        supplierRepository.delete(supplier);
        logger.info("Supplier deleted: {}", supplier.getName());
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .contactPerson(supplier.getContactPerson())
                .active(supplier.isActive())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
