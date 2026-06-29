package com.quickbite.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotNull private Long restaurantId;
    @NotBlank private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
}