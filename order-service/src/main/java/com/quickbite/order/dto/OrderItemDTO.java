package com.quickbite.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemDTO {
    private String name;
    private Double price;
    private Integer quantity;
    private String customization;
}
