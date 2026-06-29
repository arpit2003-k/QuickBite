package com.quickbite.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientId;
    private String recipientEmail;
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String message;
    private Long relatedId;  // orderId
    private String deepLinkUrl;
    private Boolean isRead = false;
    private LocalDateTime sentAt;

    public enum NotificationType {
        ORDER_PLACED, ORDER_CONFIRMED, ORDER_PREPARING, 
        ORDER_PICKED_UP, ORDER_DELIVERED, ORDER_CANCELLED,
        PROMO_BROADCAST, DELIVERY_ASSIGNED
    }
}