package com.quickbite.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double price;

    private Double discountedPrice;   // optional

    private String imageUrl;

    private Boolean isVeg = true;

    private Boolean isAvailable = true;

    private Double rating = 0.0;

    private Integer calories;

    private String tags;   // e.g., "spicy, bestseller"
}