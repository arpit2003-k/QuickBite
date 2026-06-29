package com.quickbite.cart.service;

import com.quickbite.cart.client.MenuClient;
import com.quickbite.cart.client.RestaurantClient;
import com.quickbite.cart.dto.*;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import com.quickbite.cart.exception.CustomException;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MenuClient menuClient;
    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart mockCart;
    private AddItemRequest addItemRequest;
    private MenuItemDTO mockMenuItem;

    @BeforeEach
    void setUp() {
        mockCart = new Cart();
        mockCart.setCartId(1L);
        mockCart.setCustomerId(100L);
        mockCart.setTotalPrice(0.0);
        mockCart.setItems(new ArrayList<>());

        addItemRequest = new AddItemRequest();
        addItemRequest.setCustomerId(100L);
        addItemRequest.setMenuItemId(1L);
        addItemRequest.setQuantity(2);

        mockMenuItem = new MenuItemDTO();
        mockMenuItem.setItemId(1L);
        mockMenuItem.setRestaurantId(10L);
        mockMenuItem.setPrice(100.0);
        mockMenuItem.setIsAvailable(true);
    }

    @Test
    void getCart_Success_ReturnsCart() {
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));

        CartResponse response = cartService.getCart(100L);

        assertNotNull(response);
        assertEquals(100L, response.getCustomerId());
    }

    @Test
    void addItem_Success_AddsNewItem() {
        when(menuClient.getMenuItem(anyLong())).thenReturn(mockMenuItem);
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCart_CartIdAndMenuItemId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        CartResponse response = cartService.addItem(addItemRequest);

        assertNotNull(response);
        assertEquals(10L, response.getRestaurantId());
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_DifferentRestaurant_ThrowsCustomException() {
        mockCart.setRestaurantId(20L); // Current cart belongs to restaurant 20
        when(menuClient.getMenuItem(anyLong())).thenReturn(mockMenuItem); // New item belongs to restaurant 10
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));

        assertThrows(CustomException.class, () -> cartService.addItem(addItemRequest));
    }

    @Test
    void addItem_ItemUnavailable_ThrowsCustomException() {
        mockMenuItem.setIsAvailable(false);
        when(menuClient.getMenuItem(anyLong())).thenReturn(mockMenuItem);

        assertThrows(CustomException.class, () -> cartService.addItem(addItemRequest));
    }

    @Test
    void updateQuantity_Success_UpdatesValue() {
        CartItem item = new CartItem();
        item.setMenuItemId(1L);
        item.setQuantity(1);
        item.setPrice(100.0);
        mockCart.getItems().add(item);
        
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCart_CartIdAndMenuItemId(anyLong(), anyLong())).thenReturn(Optional.of(item));

        UpdateQuantityRequest updateReq = new UpdateQuantityRequest();
        updateReq.setCustomerId(100L);
        updateReq.setMenuItemId(1L);
        updateReq.setQuantity(5);

        CartResponse response = cartService.updateQuantity(updateReq);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void removeItem_Success_RemovesItem() {
        CartItem item = new CartItem();
        item.setMenuItemId(1L);
        item.setPrice(100.0);
        item.setQuantity(1);
        mockCart.getItems().add(item);

        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCart_CartIdAndMenuItemId(anyLong(), anyLong())).thenReturn(Optional.of(item));

        cartService.removeItem(100L, 1L);

        assertTrue(mockCart.getItems().isEmpty());
        verify(cartItemRepository).delete(item);
    }

    @Test
    void clearCart_Success_EmptyCart() {
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));

        cartService.clearCart(100L);

        assertEquals(0.0, mockCart.getTotalPrice());
        assertNull(mockCart.getRestaurantId());
    }

    @Test
    void applyPromoCode_Success_ReturnsResponse() {
        mockCart.setTotalPrice(1000.0);
        CartItem item = new CartItem();
        item.setPrice(1000.0);
        item.setQuantity(1);
        mockCart.getItems().add(item);
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCustomerId(100L);
        req.setPromoCode("SAVE10"); // Logic for SAVE10 gives 10% discount

        CartResponse response = cartService.applyPromoCode(req);

        assertEquals(900.0, response.getTotalPrice());
    }

    @Test
    void applyPromoCode_InvalidCode_ThrowsCustomException() {
        mockCart.setTotalPrice(100.0);
        CartItem item = new CartItem();
        item.setPrice(100.0);
        item.setQuantity(1);
        mockCart.getItems().add(item);
        when(cartRepository.findByCustomerId(100L)).thenReturn(Optional.of(mockCart));

        ApplyPromoRequest req = new ApplyPromoRequest();
        req.setCustomerId(100L);
        req.setPromoCode("WRONG");

        assertThrows(CustomException.class, () -> cartService.applyPromoCode(req));
    }
}
