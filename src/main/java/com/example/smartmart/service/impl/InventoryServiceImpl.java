package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.InventoryAdjustmentRequest;
import com.example.smartmart.dto.response.InventoryResponse;
import com.example.smartmart.dto.response.InventoryTransactionResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.entity.Inventory;
import com.example.smartmart.entity.InventoryTransaction;
import com.example.smartmart.enumiration.TransactionType;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.InventoryRepository;
import com.example.smartmart.repository.InventoryTransactionRepository;
import com.example.smartmart.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                 InventoryTransactionRepository transactionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse findByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return mapToResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse adjust(Long productId, InventoryAdjustmentRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        int newQuantity;
        if (request.getTransactionType() == TransactionType.STOCK_IN
                || request.getTransactionType() == TransactionType.ORDER_CANCELLATION_RETURN) {
            newQuantity = inventory.getQuantityInStock() + request.getQuantity();
        } else if (request.getTransactionType() == TransactionType.ADJUSTMENT) {
            newQuantity = request.getQuantity();
        } else {
            newQuantity = inventory.getQuantityInStock() - request.getQuantity();
            if (newQuantity < 0) {
                throw new BusinessException("Insufficient stock. Available: " + inventory.getQuantityInStock());
            }
        }

        inventory.setQuantityInStock(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        InventoryTransaction txn = InventoryTransaction.builder()
                .inventory(saved)
                .transactionType(request.getTransactionType())
                .quantityChanged(request.getQuantity())
                .quantityAfter(newQuantity)
                .notes(request.getNotes())
                .build();
        transactionRepository.save(txn);

        logger.info("Inventory adjusted for product ID: {}. New stock: {}", productId, newQuantity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionResponse> getTransactionHistory(Long productId, Pageable pageable) {
        Page<InventoryTransactionResponse> page = transactionRepository
                .findByInventoryProductId(productId, pageable)
                .map(this::mapTransactionToResponse);
        return PagedResponse.of(page);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .quantityInStock(inventory.getQuantityInStock())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .lowStock(inventory.getQuantityInStock() <= inventory.getLowStockThreshold())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private InventoryTransactionResponse mapTransactionToResponse(InventoryTransaction txn) {
        return InventoryTransactionResponse.builder()
                .id(txn.getId())
                .productId(txn.getInventory().getProduct().getId())
                .productName(txn.getInventory().getProduct().getName())
                .transactionType(txn.getTransactionType())
                .quantityChanged(txn.getQuantityChanged())
                .quantityAfter(txn.getQuantityAfter())
                .notes(txn.getNotes())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
