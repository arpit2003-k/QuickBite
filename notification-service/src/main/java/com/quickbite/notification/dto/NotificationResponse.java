package com.quickbite.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long relatedId;
    private String deepLinkUrl;
    private Boolean isRead;
    private LocalDateTime sentAt;
}