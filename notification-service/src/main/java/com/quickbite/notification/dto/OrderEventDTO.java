package com.quickbite.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEventDTO {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long restaurantOwnerId;
    private String status;
    private String customerEmail;
    private String customerPhone;
    private String restaurantOwnerEmail;
    private String restaurantOwnerPhone;
    private Long deliveryAgentId;
    private String deliveryAgentEmail;
    private String deliveryAgentPhone;
    private LocalDateTime eventTime;
}