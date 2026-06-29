package com.quickbite.order.dto;

import lombok.Data;

@Data
public class RestaurantDTO {
    private Long restaurantId;
    private Long ownerId;        // ← ADD THIS FIELD
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
}