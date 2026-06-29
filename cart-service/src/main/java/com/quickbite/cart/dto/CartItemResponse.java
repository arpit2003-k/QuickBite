package com.quickbite.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long menuItemId;
    private String name;
    private Double price;
    private Integer quantity;
    private String customization;
    private Double subtotal;
}
