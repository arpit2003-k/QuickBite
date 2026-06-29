package com.quickbite.menu.controller;

import com.quickbite.menu.dto.*;
import com.quickbite.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@Slf4j
@Tag(name = "Menu", description = "Manage categories and menu items")
public class MenuController {

    @Autowired
    private MenuService menuService;

    // ========== Public endpoints (guest access) ==========
    @GetMapping("/categories/restaurant/{restaurantId}")
    @Operation(summary = "Get all categories of a restaurant")
    public ResponseEntity<List<CategoryResponse>> getCategories(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getCategoriesByRestaurant(restaurantId));
    }

    @GetMapping("/items/restaurant/{restaurantId}")
    @Operation(summary = "Get all menu items of a restaurant")
    public ResponseEntity<List<MenuItemResponse>> getItemsByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getMenuItemsByRestaurant(restaurantId));
    }

    @GetMapping("/items/category/{categoryId}")
    @Operation(summary = "Get items by category")
    public ResponseEntity<List<MenuItemResponse>> getItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(menuService.getMenuItemsByCategory(categoryId));
    }

    @GetMapping("/items/available/{restaurantId}")
    @Operation(summary = "Get only available items of a restaurant")
    public ResponseEntity<List<MenuItemResponse>> getAvailableItems(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getAvailableItemsByRestaurant(restaurantId));
    }

    @GetMapping("/items/veg/{restaurantId}")
    @Operation(summary = "Get veg items of a restaurant")
    public ResponseEntity<List<MenuItemResponse>> getVegItems(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getVegItemsByRestaurant(restaurantId));
    }

    @GetMapping("/items/search")
    @Operation(summary = "Search menu items by keyword")
    public ResponseEntity<List<MenuItemResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(menuService.searchMenuItems(keyword));
    }

    @GetMapping("/items/{itemId}")
    @Operation(summary = "Get menu item by ID")
    public ResponseEntity<MenuItemResponse> getItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(menuService.getMenuItemById(itemId));
    }

    // ========== Owner/Admin endpoints (should be protected) ==========
    @PostMapping("/categories")
    @Operation(summary = "Add new category (owner only)")
    public ResponseEntity<CategoryResponse> addCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(menuService.addCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long categoryId,
                                                            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(menuService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Delete category (and its items)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        menuService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items")
    @Operation(summary = "Add new menu item")
    public ResponseEntity<MenuItemResponse> addMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.addMenuItem(request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update menu item")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long itemId,
                                                            @Valid @RequestBody MenuItemUpdateRequest request) {
        return ResponseEntity.ok(menuService.updateMenuItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        menuService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{itemId}/availability")
    @Operation(summary = "Toggle item availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(@PathVariable Long itemId,
                                                                @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(menuService.toggleAvailability(itemId, isAvailable));
    }
}