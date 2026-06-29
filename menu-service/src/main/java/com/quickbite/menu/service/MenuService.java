package com.quickbite.menu.service;

import com.quickbite.menu.dto.*;

import java.util.List;

public interface MenuService {
    // Category operations
    CategoryResponse addCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long categoryId, CategoryRequest request);
    void deleteCategory(Long categoryId);
    List<CategoryResponse> getCategoriesByRestaurant(Long restaurantId);
    
    // Menu item operations
    MenuItemResponse addMenuItem(MenuItemRequest request);
    MenuItemResponse updateMenuItem(Long itemId, MenuItemUpdateRequest request);
    void deleteMenuItem(Long itemId);
    MenuItemResponse toggleAvailability(Long itemId, Boolean isAvailable);
    MenuItemResponse getMenuItemById(Long itemId);
    List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId);
    List<MenuItemResponse> getMenuItemsByCategory(Long categoryId);
    List<MenuItemResponse> getAvailableItemsByRestaurant(Long restaurantId);
    List<MenuItemResponse> getVegItemsByRestaurant(Long restaurantId);
    List<MenuItemResponse> searchMenuItems(String keyword);
}