package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingLocationPoint {
    private String label;
    private Double latitude;
    private Double longitude;
}
