package com.example.smartmart.service;

import com.example.smartmart.dto.request.OrderRequest;
import com.example.smartmart.dto.request.OrderStatusUpdateRequest;
import com.example.smartmart.dto.response.OrderResponse;
import com.example.smartmart.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(String email, OrderRequest request);
    PagedResponse<OrderResponse> getMyOrders(String email, Pageable pageable);
    OrderResponse getOrderById(String email, Long orderId);
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
    void cancelOrder(String email, Long orderId);
}
