package com.quickbite.cart.dto;

import lombok.Data;

@Data
public class MenuItemDTO {
    private Long itemId;
    private Long restaurantId;
    private Long categoryId;
    private String name;
    private String description;
    private Double price;
    private Double discountedPrice;
    private Boolean isVeg;
    private Boolean isAvailable;
}
