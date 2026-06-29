package com.quickbite.order.dto;

import lombok.Data;

@Data
public class DeliveryAssignmentDTO {
    private Long orderId;
    private Long restaurantId;
    private Double restaurantLat;
    private Double restaurantLng;
    private Long customerId;
    private String pickupAddress;
    private String deliveryAddress;
}
