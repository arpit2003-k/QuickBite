package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private Long agentId;
    private Long userId;
    private String fullName;
    private String phone;
    private String vehicleType;
    private String vehicleNumber;
    private Boolean isAvailable;
    private Boolean isVerified;
    private Double avgRating;
    private Integer totalDeliveries;
    private Double totalEarnings;
    private Double currentLatitude;
    private Double currentLongitude;
}
