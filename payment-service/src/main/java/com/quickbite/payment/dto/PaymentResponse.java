package com.quickbite.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String status;
    private String message;
    private String transactionId;
}
