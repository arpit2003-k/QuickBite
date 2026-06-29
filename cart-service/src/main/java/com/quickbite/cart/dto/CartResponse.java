package com.quickbite.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long customerId;
    private Long restaurantId;
    private Double totalPrice;
    private List<CartItemResponse> items;
}
