package com.quickbite.delivery.dto;

import lombok.Data;

@Data
public class AssignmentRequest {
    private Long orderId;
    private Long restaurantId;
    private Double restaurantLat;
    private Double restaurantLng;
    private String deliveryAddress;
}
