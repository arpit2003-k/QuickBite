package com.quickbite.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EarningsResponse {
    private Long agentId;
    private Double totalEarnings;
    private Integer totalDeliveries;
    private Double avgRating;
}
