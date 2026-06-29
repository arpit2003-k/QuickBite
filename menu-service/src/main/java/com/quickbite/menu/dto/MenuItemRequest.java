package com.quickbite.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuItemRequest {
    @NotNull private Long restaurantId;
    @NotNull private Long categoryId;
    @NotBlank private String name;
    private String description;
    @Positive private Double price;
    private Double discountedPrice;
    private String imageUrl;
    private Boolean isVeg;
    private Boolean isAvailable;
    private Integer calories;
    private String tags;
}