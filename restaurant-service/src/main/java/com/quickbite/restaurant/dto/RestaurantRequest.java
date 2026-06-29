package com.quickbite.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantRequest {
    
    @NotNull private Long ownerId;
    
    @NotBlank private String name;
    
    private String description;
    
    private String cuisine;
    
    @NotBlank private String address;
    
    private String city;
    
    @NotNull private Double latitude;
    
    @NotNull private Double longitude;
    
    @NotBlank private String phone;
    
    @Positive private Integer deliveryRadius;
    
    @Positive private Double minOrderAmount;
    
    private Integer estimatedDeliveryMin;
    
    private String imageUrl;
}
