package com.quickbite.restaurant.dto;

import lombok.Data;

@Data
public class NearbyRequest {
    private Double latitude;
    private Double longitude;
    private Double radius;   // in km, default 10
}
