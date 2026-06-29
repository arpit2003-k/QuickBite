package com.quickbite.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyPromoRequest {
    @NotNull private Long customerId;
    @NotBlank private String promoCode;
}
