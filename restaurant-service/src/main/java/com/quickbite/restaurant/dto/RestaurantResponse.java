package com.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestaurantResponse {
    private Long restaurantId;
    private Long ownerId;
    private String name;
    private String description;
    private String cuisine;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private Double avgRating;
    private Boolean isOpen;
    private Boolean isApproved;
    private Integer deliveryRadius;
    private Double minOrderAmount;
    private Integer estimatedDeliveryMin;
    private String imageUrl;
}
