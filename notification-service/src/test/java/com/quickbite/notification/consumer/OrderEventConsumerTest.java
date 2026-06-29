package com.quickbite.notification.consumer;

import com.quickbite.notification.dto.OrderEventDTO;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import com.quickbite.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Use reflection to set private field frontendUrl
        Field frontendUrlField = OrderEventConsumer.class.getDeclaredField("frontendUrl");
        frontendUrlField.setAccessible(true);
        frontendUrlField.set(orderEventConsumer, "http://localhost:4200");
    }

    @Test
    void testConsumeOrderEventNewNotification() {
        OrderEventDTO event = new OrderEventDTO();
        event.setOrderId(1L);
        event.setCustomerId(10L);
        event.setStatus("PLACED");
        event.setCustomerEmail("customer@test.com");
        event.setCustomerPhone("1234567890");
        event.setEventTime(LocalDateTime.now());

        when(notificationRepository.existsByRecipientIdAndRelatedIdAndType(
                eq(10L), eq(1L), eq(Notification.NotificationType.ORDER_PLACED)
        )).thenReturn(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderEventConsumer.consumeOrderEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testConsumeOrderEventDuplicateNotification() {
        OrderEventDTO event = new OrderEventDTO();
        event.setOrderId(1L);
        event.setCustomerId(10L);
        event.setStatus("PLACED");
        event.setCustomerEmail("customer@test.com");
        event.setCustomerPhone("1234567890");
        event.setEventTime(LocalDateTime.now());

        when(notificationRepository.existsByRecipientIdAndRelatedIdAndType(
                eq(10L), eq(1L), eq(Notification.NotificationType.ORDER_PLACED)
        )).thenReturn(true);

        orderEventConsumer.consumeOrderEvent(event);

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
