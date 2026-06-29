package com.quickbite.notification.controller;

import com.quickbite.notification.dto.NotificationResponse;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import com.quickbite.notification.client.AuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserNotifications() {
        Long userId = 1L;
        Notification notification = new Notification();
        notification.setId(10L);
        notification.setRecipientId(userId);
        notification.setType(Notification.NotificationType.ORDER_PLACED);
        notification.setTitle("Order Placed");
        notification.setMessage("Your order has been placed");
        notification.setIsRead(false);

        when(notificationRepository.findByRecipientIdOrderBySentAtDesc(userId))
                .thenReturn(Collections.singletonList(notification));

        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ORDER_PLACED", response.getBody().get(0).getType());
    }

    @Test
    void testGetUnreadCount() {
        Long userId = 1L;
        when(notificationRepository.countByRecipientIdAndIsReadFalse(userId)).thenReturn(5L);

        ResponseEntity<Long> response = notificationController.getUnreadCount(userId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(5L, response.getBody());
    }

    @Test
    void testMarkAllAsRead() {
        Long userId = 1L;
        doNothing().when(notificationRepository).markAllAsRead(userId);

        ResponseEntity<Void> response = notificationController.markAllAsRead(userId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(notificationRepository, times(1)).markAllAsRead(userId);
    }

    @Test
    void testMarkAsRead() {
        Long notificationId = 10L;
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        ResponseEntity<Void> response = notificationController.markAsRead(notificationId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(notification.getIsRead());
        verify(notificationRepository, times(1)).save(notification);
    }
}
