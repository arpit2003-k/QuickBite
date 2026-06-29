package com.quickbite.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddItemRequest {
    @NotNull private Long customerId;
    @NotNull private Long menuItemId;
    @Positive private Integer quantity = 1;
    private String customization;
}
