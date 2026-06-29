package com.quickbite.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AgentRegistrationRequest {
    @NotNull private Long userId;
    @NotBlank private String fullName;
    @Pattern(regexp = "^[0-9]{10}$") private String phone;
    @NotBlank private String vehicleType;
    @NotBlank private String vehicleNumber;
}
