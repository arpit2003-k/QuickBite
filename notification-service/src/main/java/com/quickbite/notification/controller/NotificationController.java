package com.quickbite.notification.controller;

import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.dto.UserDTO;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import com.quickbite.notification.client.AuthClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final AuthClient authClient;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        if (userId == 0) {
            return ResponseEntity.ok(
                notificationRepository.findAll()
                    .stream().map(n -> toResponse(n)).collect(Collectors.toList())
            );
        }
        return ResponseEntity.ok(
            notificationRepository.findByRecipientIdOrderBySentAtDesc(userId)
                .stream().map(n -> toResponse(n)).collect(Collectors.toList())
        );
    }

    @GetMapping("/debug-all")
    @Operation(summary = "Get all notifications in system for debugging")
    public ResponseEntity<List<NotificationResponse>> getAllNotificationsDebug() {
        return ResponseEntity.ok(
            notificationRepository.findAll()
                .stream().map(n -> toResponse(n)).collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.countByRecipientIdAndIsReadFalse(userId));
    }

    @PutMapping("/user/{userId}/mark-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark single notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/broadcast")
    @Operation(summary = "Admin broadcast notification to all users")
    public ResponseEntity<Void> broadcast(@RequestParam String title, @RequestParam String message) {
        log.info("📢 BROADCAST START: {} - {}", title, message);
        try {
            List<UserDTO> allUsers = authClient.getAllUsers();
            if (allUsers != null && !allUsers.isEmpty()) {
                for (UserDTO user : allUsers) {
                    Notification n = new Notification();
                    n.setRecipientId(user.getId() != null ? user.getId() : user.getUserId());
                    n.setRecipientEmail(user.getEmail());
                    n.setRecipientPhone(user.getPhone());
                    n.setType(Notification.NotificationType.PROMO_BROADCAST);
                    n.setTitle(title);
                    n.setMessage(message);
                    n.setIsRead(false);
                    n.setSentAt(LocalDateTime.now());
                    notificationRepository.save(n);
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast notifications: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
            n.getId(), n.getType().name(), n.getTitle(), n.getMessage(),
            n.getRelatedId(), n.getDeepLinkUrl(), n.getIsRead(), n.getSentAt()
        );
    }
}
