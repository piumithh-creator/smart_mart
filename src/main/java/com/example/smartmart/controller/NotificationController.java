package com.example.smartmart.controller;

import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.NotificationResponse;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "User notification endpoints (USER)")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get my notifications")
    public ResponseEntity<CommonResponse> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<NotificationResponse> response =
                notificationService.getMyNotifications(userDetails.getUsername(), PageRequest.of(page, size));
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Notifications retrieved"));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<CommonResponse> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<CommonResponse> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), null, "All notifications marked as read"));
    }
}
