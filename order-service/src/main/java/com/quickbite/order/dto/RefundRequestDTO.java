package com.quickbite.order.dto;

import lombok.Data;

@Data
public class RefundRequestDTO {
    private Long orderId;
    private Long customerId;
    private Double amount;
}
