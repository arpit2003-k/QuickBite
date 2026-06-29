package com.quickbite.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private String currency;
    private Integer amount;
    private String keyId;
}
