package com.quickbite.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private Long ownerId;   // User ID from auth-service (RESTAURANT_OWNER role)

    @Column(nullable = false)
    private String name;

    private String description;

    private String cuisine;   // e.g., "Italian, Chinese, Indian"

    private String address;

    private String city;

    private Double latitude;

    private Double longitude;

    private String phone;

    private Double avgRating = 0.0;

    private Boolean isOpen = false;   // Currently open or closed

    private Boolean isApproved = false;   // Admin approval required

    private Integer deliveryRadius;   // in kilometers

    private Double minOrderAmount;

    private Integer estimatedDeliveryMin;   // minutes

    private String imageUrl;
}
