package com.quickbite.menu.dto;

import lombok.Data;

@Data
public class MenuItemUpdateRequest {
    private String name;
    private String description;
    private Double price;
    private Double discountedPrice;
    private String imageUrl;
    private Boolean isVeg;
    private Boolean isAvailable;
    private Integer calories;
    private String tags;
}