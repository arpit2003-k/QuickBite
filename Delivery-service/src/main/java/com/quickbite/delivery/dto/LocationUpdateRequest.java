package com.quickbite.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateRequest {
    @NotNull private Long agentId;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
}
