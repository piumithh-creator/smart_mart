package com.example.smartmart.service;

import com.example.smartmart.dto.response.NotificationResponse;
import com.example.smartmart.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PagedResponse<NotificationResponse> getMyNotifications(String email, Pageable pageable);
    NotificationResponse markAsRead(String email, Long notificationId);
    void markAllAsRead(String email);
}
