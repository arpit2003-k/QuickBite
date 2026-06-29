package com.quickbite.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long agentId;

    @Column(unique = true, nullable = false)
    private Long userId;

    private String fullName;
    private String phone;
    private String vehicleType;
    private String vehicleNumber;

    private Double currentLatitude;
    private Double currentLongitude;

    private Boolean isAvailable = false;
    private Boolean isVerified = false;

    private Double avgRating = 0.0;
    private Integer totalDeliveries = 0;
    private Double totalEarnings = 0.0;

    private LocalDateTime registeredAt;
    private LocalDateTime verifiedAt;
}
