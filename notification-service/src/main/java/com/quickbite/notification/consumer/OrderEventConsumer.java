package com.quickbite.notification.consumer;

import com.quickbite.notification.dto.OrderEventDTO;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import com.quickbite.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderEventConsumer {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @RabbitListener(queues = "order_notification_queue")
    @Transactional
    public void consumeOrderEvent(OrderEventDTO event) {
        log.info("📨 Received order event: Order {} - {}", event.getOrderId(), event.getStatus());

        String title = getTitle(event.getStatus());
        String message = getMessage(event.getStatus(), event.getOrderId());
        String deepLink = frontendUrl + "/customer/track/" + event.getOrderId();

        // 1. Save notification for CUSTOMER
        saveAndSendNotification(
            event.getCustomerId(), 
            event.getCustomerEmail(), 
            event.getCustomerPhone(),
            title, message, deepLink, event.getOrderId(), event.getStatus()
        );

        // 2. Save notification for RESTAURANT OWNER (on status changes)
        if (event.getRestaurantOwnerId() != null) {
            String ownerTitle = "Order Status Update: " + event.getStatus();
            String ownerMessage = "Order #" + event.getOrderId() + " has been updated to " + event.getStatus() + ".";
            String ownerDeepLink = frontendUrl + "/owner/dashboard";
            
            saveAndSendNotification(
                event.getRestaurantOwnerId(),
                event.getRestaurantOwnerEmail(),
                event.getRestaurantOwnerPhone(),
                ownerTitle, ownerMessage, ownerDeepLink, event.getOrderId(), event.getStatus()
            );
        }

        // 3. Notify delivery agent only once when an order is assigned.
        if (event.getDeliveryAgentId() != null && "PLACED".equals(event.getStatus())) {
            String agentTitle = "Delivery Task Assigned";
            String agentMessage = "Order #" + event.getOrderId() + " has been assigned to you for delivery.";
            String agentDeepLink = frontendUrl + "/agent/dashboard";
            
            saveAndSendNotification(
                event.getDeliveryAgentId(),
                event.getDeliveryAgentEmail(),
                event.getDeliveryAgentPhone(),
                agentTitle, agentMessage, agentDeepLink, event.getOrderId(), event.getStatus()
            );
        }
    }

    private void saveAndSendNotification(Long recipientId, String email, String phone, 
                                         String title, String message, String deepLink, 
                                         Long orderId, String status) {
        if (recipientId == null) return;
        
        Notification.NotificationType type = mapStatusToType(status);
        boolean exists = notificationRepository.existsByRecipientIdAndRelatedIdAndType(recipientId, orderId, type);
        if (exists) {
            log.info("Notification already exists for recipient {}, orderId {}, type {}. Skipping to prevent duplication.", recipientId, orderId, type);
            return;
        }

        // Save to database (in-app notification)
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientEmail(email);
        notification.setRecipientPhone(phone);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(orderId);
        notification.setDeepLinkUrl(deepLink);
        notification.setIsRead(false);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Send Email
        try {
            emailService.sendEmail(email, title, message + "\n\n🔗 " + deepLink);
        } catch (Exception e) {
            log.error("Email failed: {}", e.getMessage());
        }

        // Send SMS (simulated)
        log.info("📱 SMS would be sent to: {} - {}", phone, message);
    }

    private String getTitle(String status) {
        return switch (status) {
            case "PLACED" -> "Order Placed";
            case "CONFIRMED" -> "Order Confirmed";
            case "PREPARING" -> "Food Being Prepared";
            case "PICKED_UP" -> "Order Picked Up";
            case "DELIVERED" -> "Order Delivered";
            case "CANCELLED" -> "Order Cancelled";
            default -> "Order Update";
        };
    }

    private String getMessage(String status, Long orderId) {
        return switch (status) {
            case "PLACED" -> "✅ Order #" + orderId + " placed successfully. Restaurant will confirm soon.";
            case "CONFIRMED" -> "✅ Restaurant confirmed order #" + orderId + ". Food preparation started.";
            case "PREPARING" -> "🍳 Your order #" + orderId + " is being prepared.";
            case "PICKED_UP" -> "🛵 Delivery agent picked up order #" + orderId + ".";
            case "DELIVERED" -> "🎉 Order #" + orderId + " delivered! Enjoy your meal and leave a review.";
            case "CANCELLED" -> "❌ Order #" + orderId + " cancelled. Refund will be processed.";
            default -> "Update on order #" + orderId;
        };
    }

    private Notification.NotificationType mapStatusToType(String status) {
        return switch (status) {
            case "PLACED" -> Notification.NotificationType.ORDER_PLACED;
            case "CONFIRMED" -> Notification.NotificationType.ORDER_CONFIRMED;
            case "PREPARING" -> Notification.NotificationType.ORDER_PREPARING;
            case "PICKED_UP" -> Notification.NotificationType.ORDER_PICKED_UP;
            case "DELIVERED" -> Notification.NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> Notification.NotificationType.ORDER_CANCELLED;
            case "DELIVERY_ASSIGNED" -> Notification.NotificationType.DELIVERY_ASSIGNED;
            default -> Notification.NotificationType.ORDER_PLACED;
        };
    }
}
