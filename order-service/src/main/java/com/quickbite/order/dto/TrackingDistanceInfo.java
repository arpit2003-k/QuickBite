package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDistanceInfo {
    private Double distanceKm;
    private Integer estimatedMinutes;
    private String source;
}
