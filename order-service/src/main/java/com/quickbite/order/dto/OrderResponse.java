package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private Long restaurantId;
    private Long deliveryAgentId;
    private Double totalAmount;
    private Double discount;
    private Double finalAmount;
    private String modeOfPayment;
    private String orderStatus;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private Double customerLatitude;
    private Double customerLongitude;
    private List<OrderItemDTO> items;
}
