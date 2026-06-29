package com.quickbite.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletPaymentRequest {
    @NotNull private Long customerId;
    @NotNull private Long orderId;
    @NotNull private Double amount;
}
