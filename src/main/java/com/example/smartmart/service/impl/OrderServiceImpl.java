package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.OrderRequest;
import com.example.smartmart.dto.request.OrderStatusUpdateRequest;
import com.example.smartmart.dto.response.*;
import com.example.smartmart.entity.*;
import com.example.smartmart.enumiration.*;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.*;
import com.example.smartmart.service.OrderService;
import com.example.smartmart.util.OrderNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderServiceImpl(OrderRepository orderRepository,
                             CustomerRepository customerRepository,
                             CartRepository cartRepository,
                             InventoryRepository inventoryRepository,
                             InventoryTransactionRepository inventoryTransactionRepository,
                             PaymentRepository paymentRepository,
                             NotificationRepository notificationRepository,
                             ProductDiscountRepository productDiscountRepository,
                             OrderNumberGenerator orderNumberGenerator) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.productDiscountRepository = productDiscountRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException("Cart is empty. Please add items before placing an order"));

        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException("Cart is empty. Please add items before placing an order");
        }

        // Validate stock and calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            Inventory inventory = inventoryRepository.findByProductId(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("Inventory not found for product: "
                            + cartItem.getProduct().getName()));
            if (inventory.getQuantityInStock() < cartItem.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + cartItem.getProduct().getName()
                        + ". Available: " + inventory.getQuantityInStock());
            }
            totalAmount = totalAmount.add(
                    cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // Calculate discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getCartItems()) {
            List<ProductDiscount> activeDiscounts = productDiscountRepository
                    .findActiveDiscountsForProduct(cartItem.getProduct().getId(), LocalDate.now());
            if (!activeDiscounts.isEmpty()) {
                BigDecimal bestDiscount = activeDiscounts.stream()
                        .map(pd -> pd.getDiscount().getPercentage())
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal itemTotal = cartItem.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                discountAmount = discountAmount.add(
                        itemTotal.multiply(bestDiscount).divide(BigDecimal.valueOf(100)));
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumberGenerator.generate())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .build();
        Order savedOrder = orderRepository.save(order);

        // Create order items and deduct inventory
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProduct().getPrice())
                    .subtotal(cartItem.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            savedOrder.getOrderItems().add(orderItem);

            Inventory inventory = inventoryRepository.findByProductId(cartItem.getProduct().getId()).get();
            int newQty = inventory.getQuantityInStock() - cartItem.getQuantity();
            inventory.setQuantityInStock(newQty);
            Inventory savedInv = inventoryRepository.save(inventory);

            InventoryTransaction txn = InventoryTransaction.builder()
                    .inventory(savedInv)
                    .transactionType(TransactionType.ORDER_DEDUCTION)
                    .quantityChanged(cartItem.getQuantity())
                    .quantityAfter(newQty)
                    .notes("Order: " + savedOrder.getOrderNumber())
                    .build();
            inventoryTransactionRepository.save(txn);
        }

        orderRepository.save(savedOrder);

        // Create payment record
        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(finalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Clear cart
        cart.getCartItems().clear();
        cartRepository.save(cart);

        // Send notification
        Notification notification = Notification.builder()
                .customer(customer)
                .notificationType(NotificationType.ORDER_PLACED)
                .title("Order Placed Successfully")
                .message("Your order " + savedOrder.getOrderNumber() + " has been placed. Total: " + finalAmount)
                .read(false)
                .build();
        notificationRepository.save(notification);

        logger.info("Order placed: {} for customer: {}", savedOrder.getOrderNumber(), email);
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyOrders(String email, Pageable pageable) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        Page<OrderResponse> page = orderRepository.findByCustomerId(customer.getId(), pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Admin can access any order; customers only their own
        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            Customer customer = customerRepository.findByUserEmail(email).orElse(null);
            if (customer == null || !order.getCustomer().getId().equals(customer.getId())) {
                throw new BusinessException("You are not authorized to view this order");
            }
        }
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(request.getStatus());

        // Send notification based on status
        NotificationType notifType = switch (request.getStatus()) {
            case CONFIRMED -> NotificationType.ORDER_CONFIRMED;
            case SHIPPED -> NotificationType.ORDER_SHIPPED;
            case DELIVERED -> NotificationType.ORDER_DELIVERED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.GENERAL;
        };

        Notification notification = Notification.builder()
                .customer(order.getCustomer())
                .notificationType(notifType)
                .title("Order " + request.getStatus().name())
                .message("Your order " + order.getOrderNumber() + " status is now: " + request.getStatus().name())
                .read(false)
                .build();
        notificationRepository.save(notification);

        logger.info("Order {} status updated to: {}", order.getOrderNumber(), request.getStatus());
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel an order that has been shipped or delivered");
        }

        // Return inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryRepository.findByProductId(item.getProduct().getId()).ifPresent(inventory -> {
                int newQty = inventory.getQuantityInStock() + item.getQuantity();
                inventory.setQuantityInStock(newQty);
                Inventory saved = inventoryRepository.save(inventory);

                InventoryTransaction txn = InventoryTransaction.builder()
                        .inventory(saved)
                        .transactionType(TransactionType.ORDER_CANCELLATION_RETURN)
                        .quantityChanged(item.getQuantity())
                        .quantityAfter(newQty)
                        .notes("Cancelled order: " + order.getOrderNumber())
                        .build();
                inventoryTransactionRepository.save(txn);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Notification notification = Notification.builder()
                .customer(order.getCustomer())
                .notificationType(NotificationType.ORDER_CANCELLED)
                .title("Order Cancelled")
                .message("Your order " + order.getOrderNumber() + " has been cancelled")
                .read(false)
                .build();
        notificationRepository.save(notification);

        logger.info("Order cancelled: {} by: {}", order.getOrderNumber(), email);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        PaymentResponse paymentResponse = null;
        if (order.getPayment() != null) {
            Payment p = order.getPayment();
            paymentResponse = PaymentResponse.builder()
                    .id(p.getId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .amount(p.getAmount())
                    .paymentMethod(p.getPaymentMethod())
                    .paymentStatus(p.getPaymentStatus())
                    .transactionReference(p.getTransactionReference())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerEmail(order.getCustomer().getUser().getEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .orderItems(itemResponses)
                .payment(paymentResponse)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
