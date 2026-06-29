package com.quickbite.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull private Long orderId;
    @NotNull private Long customerId;
    @NotNull private Double amount;
    @NotNull private String mode;   // COD, CARD, UPI, WALLET
}
