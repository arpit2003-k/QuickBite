package com.quickbite.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private Long restaurantId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
}