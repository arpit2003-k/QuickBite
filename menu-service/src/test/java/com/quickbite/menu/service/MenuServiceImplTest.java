package com.quickbite.menu.service;

import com.quickbite.menu.client.RestaurantClient;
import com.quickbite.menu.dto.*;
import com.quickbite.menu.entity.MenuCategory;
import com.quickbite.menu.entity.MenuItem;
import com.quickbite.menu.exception.CustomException;
import com.quickbite.menu.repository.MenuCategoryRepository;
import com.quickbite.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuCategoryRepository categoryRepository;
    @Mock
    private MenuItemRepository itemRepository;
    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private MenuServiceImpl menuService;

    private MenuCategory mockCategory;
    private MenuItem mockItem;

    @BeforeEach
    void setUp() {
        mockCategory = new MenuCategory();
        mockCategory.setCategoryId(1L);
        mockCategory.setRestaurantId(1L);
        mockCategory.setName("Starters");

        mockItem = new MenuItem();
        mockItem.setItemId(10L);
        mockItem.setRestaurantId(1L);
        mockItem.setCategoryId(1L);
        mockItem.setName("Paneer Tikka");
        mockItem.setPrice(250.0);
        mockItem.setIsAvailable(true);
    }

    @Test
    void addCategory_Success_ReturnsResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setRestaurantId(1L);
        request.setName("New Cat");
        when(categoryRepository.existsByRestaurantIdAndName(anyLong(), anyString())).thenReturn(false);
        when(categoryRepository.save(any(MenuCategory.class))).thenReturn(mockCategory);

        CategoryResponse response = menuService.addCategory(request);

        assertNotNull(response);
        verify(restaurantClient).getRestaurant(1L);
    }

    @Test
    void addCategory_Duplicate_ThrowsCustomException() {
        CategoryRequest request = new CategoryRequest();
        request.setRestaurantId(1L);
        request.setName("Starters");
        when(categoryRepository.existsByRestaurantIdAndName(1L, "Starters")).thenReturn(true);

        assertThrows(CustomException.class, () -> menuService.addCategory(request));
    }

    @Test
    void updateCategory_Success_ReturnsResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.save(any(MenuCategory.class))).thenReturn(mockCategory);

        CategoryResponse response = menuService.updateCategory(1L, new CategoryRequest());

        assertNotNull(response);
    }

    @Test
    void deleteCategory_Success_CallsDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByCategoryId(1L)).thenReturn(Collections.emptyList());

        menuService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void getCategoriesByRestaurant_Success_ReturnsList() {
        when(categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(1L)).thenReturn(Collections.singletonList(mockCategory));

        List<CategoryResponse> results = menuService.getCategoriesByRestaurant(1L);

        assertEquals(1, results.size());
    }

    @Test
    void addMenuItem_Success_ReturnsResponse() {
        MenuItemRequest request = new MenuItemRequest();
        request.setRestaurantId(1L);
        request.setCategoryId(1L);
        request.setName("Dosa");
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.save(any(MenuItem.class))).thenReturn(mockItem);

        MenuItemResponse response = menuService.addMenuItem(request);

        assertNotNull(response);
        verify(restaurantClient).getRestaurant(1L);
    }

    @Test
    void addMenuItem_CategoryNotFound_ThrowsCustomException() {
        MenuItemRequest request = new MenuItemRequest();
        request.setCategoryId(99L);
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(CustomException.class, () -> menuService.addMenuItem(request));
    }

    @Test
    void toggleAvailability_Success_UpdatesStatus() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(MenuItem.class))).thenReturn(mockItem);

        MenuItemResponse response = menuService.toggleAvailability(10L, false);

        assertFalse(response.getIsAvailable());
    }

    @Test
    void updateMenuItem_Success_ReturnsResponse() {
        MenuItemUpdateRequest request = new MenuItemUpdateRequest();
        request.setName("Updated Item");
        when(itemRepository.findById(10L)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(MenuItem.class))).thenReturn(mockItem);

        MenuItemResponse response = menuService.updateMenuItem(10L, request);

        assertNotNull(response);
    }

    @Test
    void deleteMenuItem_Success_CallsDelete() {
        when(itemRepository.existsById(10L)).thenReturn(true);

        menuService.deleteMenuItem(10L);

        verify(itemRepository).deleteById(10L);
    }

    @Test
    void getMenuItemById_Success_ReturnsResponse() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(mockItem));

        MenuItemResponse response = menuService.getMenuItemById(10L);

        assertEquals(10L, response.getItemId());
    }

    @Test
    void getMenuItemsByRestaurant_Success_ReturnsList() {
        when(itemRepository.findByRestaurantId(1L)).thenReturn(Collections.singletonList(mockItem));

        List<MenuItemResponse> results = menuService.getMenuItemsByRestaurant(1L);

        assertEquals(1, results.size());
    }

    @Test
    void searchMenuItems_Success_ReturnsList() {
        when(itemRepository.findByNameContainingIgnoreCase("Paneer")).thenReturn(Collections.singletonList(mockItem));

        List<MenuItemResponse> results = menuService.searchMenuItems("Paneer");

        assertEquals(1, results.size());
    }

    @Test
    void getMenuItemsByCategory_Success_ReturnsList() {
        when(itemRepository.findByCategoryId(1L)).thenReturn(Collections.singletonList(mockItem));

        List<MenuItemResponse> results = menuService.getMenuItemsByCategory(1L);

        assertEquals(1, results.size());
    }

    @Test
    void getAvailableItemsByRestaurant_Success_ReturnsList() {
        when(itemRepository.findByRestaurantIdAndIsAvailableTrue(1L)).thenReturn(Collections.singletonList(mockItem));

        List<MenuItemResponse> results = menuService.getAvailableItemsByRestaurant(1L);

        assertEquals(1, results.size());
    }

    @Test
    void getVegItemsByRestaurant_Success_ReturnsList() {
        when(itemRepository.findByRestaurantIdAndIsVegTrue(1L)).thenReturn(Collections.singletonList(mockItem));

        List<MenuItemResponse> results = menuService.getVegItemsByRestaurant(1L);

        assertEquals(1, results.size());
    }
}
