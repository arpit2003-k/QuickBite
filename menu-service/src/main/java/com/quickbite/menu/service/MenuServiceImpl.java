package com.quickbite.menu.service;

import com.quickbite.menu.client.RestaurantClient;
import com.quickbite.menu.dto.*;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import com.quickbite.menu.exception.CustomException;
import com.quickbite.menu.repository.MenuCategoryRepository;
import com.quickbite.menu.repository.MenuItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuCategoryRepository categoryRepository;
    
    @Autowired
    private MenuItemRepository itemRepository;
    
    @Autowired
    private RestaurantClient restaurantClient;   // ← Inject Feign client

    // Helper conversions
    private CategoryResponse toCategoryResponse(MenuCategory c) {
        return new CategoryResponse(c.getCategoryId(), c.getRestaurantId(), c.getName(),
                c.getDescription(), c.getImageUrl(), c.getDisplayOrder());
    }

    private MenuItemResponse toItemResponse(MenuItem i) {
        return new MenuItemResponse(i.getItemId(), i.getRestaurantId(), i.getCategoryId(),
                i.getName(), i.getDescription(), i.getPrice(), i.getDiscountedPrice(),
                i.getImageUrl(), i.getIsVeg(), i.getIsAvailable(), i.getRating(),
                i.getCalories(), i.getTags());
    }

    // ========== Category ==========
    @Override
    @Transactional
    public CategoryResponse addCategory(CategoryRequest request) {
        log.info("Adding category: {} for restaurant {}", request.getName(), request.getRestaurantId());
        if (categoryRepository.existsByRestaurantIdAndName(request.getRestaurantId(), request.getName())) {
            throw new CustomException("Category already exists for this restaurant");
        }
        try {
            restaurantClient.getRestaurant(request.getRestaurantId());
        } catch (Exception e) {
        	throw new CustomException("Restaurant not found with ID: " + request.getRestaurantId());
        }
        
   
        MenuCategory category = new MenuCategory();
        category.setRestaurantId(request.getRestaurantId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder(request.getDisplayOrder());
        MenuCategory saved = categoryRepository.save(category);
        
        
        return toCategoryResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        log.info("Updating category: {}", categoryId);
        MenuCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("Category not found"));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder(request.getDisplayOrder());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: {}", categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new CustomException("Category not found");
        }
        // Also delete items under this category? Or set categoryId to null? For simplicity, delete items.
        List<MenuItem> items = itemRepository.findByCategoryId(categoryId);
        itemRepository.deleteAll(items);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryResponse> getCategoriesByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
                .stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    // ========== Menu Items ==========
    @Override
    @Transactional
    public MenuItemResponse addMenuItem(MenuItemRequest request) {
        log.info("Adding menu item: {} to restaurant {}", request.getName(), request.getRestaurantId());
        try {
            restaurantClient.getRestaurant(request.getRestaurantId());
        } catch (Exception e) {
            throw new CustomException("Restaurant not found with ID: " + request.getRestaurantId());
        }
        
        
        // Verify category exists
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new CustomException("Category not found");
        }
        MenuItem item = new MenuItem();
        item.setRestaurantId(request.getRestaurantId());
        item.setCategoryId(request.getCategoryId());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setDiscountedPrice(request.getDiscountedPrice());
        item.setImageUrl(request.getImageUrl());
        item.setIsVeg(request.getIsVeg() != null ? request.getIsVeg() : true);
        item.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        item.setCalories(request.getCalories());
        item.setTags(request.getTags());
        MenuItem saved = itemRepository.save(item);
        return toItemResponse(saved);
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemUpdateRequest request) {
        log.info("Updating menu item: {}", itemId);
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException("Menu item not found"));
        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getDiscountedPrice() != null) item.setDiscountedPrice(request.getDiscountedPrice());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsVeg() != null) item.setIsVeg(request.getIsVeg());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());
        if (request.getCalories() != null) item.setCalories(request.getCalories());
        if (request.getTags() != null) item.setTags(request.getTags());
        return toItemResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long itemId) {
        log.info("Deleting menu item: {}", itemId);
        if (!itemRepository.existsById(itemId)) {
            throw new CustomException("Menu item not found");
        }
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public MenuItemResponse toggleAvailability(Long itemId, Boolean isAvailable) {
        log.info("Toggling availability for item {}: {}", itemId, isAvailable);
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException("Menu item not found"));
        item.setIsAvailable(isAvailable);
        return toItemResponse(itemRepository.save(item));
    }

    @Override
    public MenuItemResponse getMenuItemById(Long itemId) {
        return toItemResponse(itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException("Menu item not found")));
    }

    @Override
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        return itemRepository.findByRestaurantId(restaurantId)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Override
    public List<MenuItemResponse> getMenuItemsByCategory(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Override
    public List<MenuItemResponse> getAvailableItemsByRestaurant(Long restaurantId) {
        return itemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Override
    public List<MenuItemResponse> getVegItemsByRestaurant(Long restaurantId) {
        return itemRepository.findByRestaurantIdAndIsVegTrue(restaurantId)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    @Override
    public List<MenuItemResponse> searchMenuItems(String keyword) {
        return itemRepository.findByNameContainingIgnoreCase(keyword)
                .stream().map(this::toItemResponse).collect(Collectors.toList());
    }
}