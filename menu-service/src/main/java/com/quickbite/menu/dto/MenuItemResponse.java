package com.quickbite.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MenuItemResponse {
    private Long itemId;
    private Long restaurantId;
    private Long categoryId;
    private String name;
    private String description;
    private Double price;
    private Double discountedPrice;
    private String imageUrl;
    private Boolean isVeg;
    private Boolean isAvailable;
    private Double rating;
    private Integer calories;
    private String tags;
}