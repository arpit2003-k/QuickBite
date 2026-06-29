package com.quickbite.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMoneyRequest {
    @NotNull private Long customerId;
    @NotNull private Double amount;
    private String description;   // optional
}
