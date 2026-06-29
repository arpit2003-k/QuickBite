package com.quickbite.order.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long orderId;
    private Long customerId;
    private Double amount;
    private String mode;
}
