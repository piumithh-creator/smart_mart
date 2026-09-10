package com.example.smartmart.service.impl;

import com.example.smartmart.dto.response.DashboardResponse;
import com.example.smartmart.enumiration.OrderStatus;
import com.example.smartmart.repository.*;
import com.example.smartmart.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public AdminServiceImpl(ProductRepository productRepository,
                             CustomerRepository customerRepository,
                             OrderRepository orderRepository,
                             InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        return DashboardResponse.builder()
                .totalProducts(productRepository.countByActiveTrue())
                .totalCustomers(customerRepository.count())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .completedOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .totalSales(orderRepository.sumTotalSales())
                .lowStockProducts(inventoryRepository.countLowStockItems())
                .build();
    }
}
