package com.example.smartmart.dto.response;

import com.example.smartmart.enumiration.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType notificationType;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
