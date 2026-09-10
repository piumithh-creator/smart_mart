package com.example.smartmart.service.impl;

import com.example.smartmart.dto.response.NotificationResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.entity.Customer;
import com.example.smartmart.entity.Notification;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.CustomerRepository;
import com.example.smartmart.repository.NotificationRepository;
import com.example.smartmart.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                    CustomerRepository customerRepository) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getMyNotifications(String email, Pageable pageable) {
        Customer customer = getCustomer(email);
        Page<NotificationResponse> page = notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customer.getId(), pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        Customer customer = getCustomer(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("You are not authorized to update this notification");
        }

        notification.setRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        Customer customer = getCustomer(email);
        notificationRepository.markAllAsReadByCustomerId(customer.getId());
    }

    private Customer getCustomer(String email) {
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
