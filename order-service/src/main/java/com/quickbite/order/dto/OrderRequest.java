package com.quickbite.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull private Long customerId;
    @NotBlank private String deliveryAddress;
    private Double customerLatitude;
    private Double customerLongitude;
    private String specialInstructions;
    @NotBlank private String paymentMode;   // COD, WALLET, CARD, UPI
}
