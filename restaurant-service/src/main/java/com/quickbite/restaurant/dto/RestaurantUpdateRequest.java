package com.quickbite.restaurant.dto;

import lombok.Data;

@Data
public class RestaurantUpdateRequest {
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private Boolean isOpen;
    private Integer deliveryRadius;
    private Double minOrderAmount;
    private Integer estimatedDeliveryMin;
    private String imageUrl;
}
