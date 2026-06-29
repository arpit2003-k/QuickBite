package com.quickbite.cart.service;

import com.quickbite.cart.client.MenuClient;
import com.quickbite.cart.client.RestaurantClient;
import com.quickbite.cart.dto.*;
import com.quickbite.cart.entity.Cart;
import com.quickbite.cart.entity.CartItem;
import com.quickbite.cart.exception.CustomException;
import com.quickbite.cart.repository.CartItemRepository;
import com.quickbite.cart.repository.CartRepository;
import com.quickbite.cart.util.PromoValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired
    private MenuClient menuClient;

    // Helper: convert Cart entity to CartResponse
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getCartItemId(),
                        item.getMenuItemId(),
                        item.getName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getCustomization(),
                        item.getPrice() * item.getQuantity()
                ))
                .collect(Collectors.toList());
        return new CartResponse(
                cart.getCartId(),
                cart.getCustomerId(),
                cart.getRestaurantId(),
                cart.getTotalPrice(),
                itemResponses
        );
    }

    // Get or create cart for customer
    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setTotalPrice(0.0);
                    newCart.setRestaurantId(null);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    // Recalculate total price of cart
    private void recalculateTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(total);
        cartRepository.save(cart);
    }

    @Override
    public CartResponse getCart(Long customerId) {
        log.info("Fetching cart for customer: {}", customerId);
        Cart cart = getOrCreateCart(customerId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(AddItemRequest request) {
        log.info("Adding item {} to cart for customer {}", request.getMenuItemId(), request.getCustomerId());

        // 1. Fetch menu item from Menu Service (Feign)
        MenuItemDTO menuItem;
        try {
            menuItem = menuClient.getMenuItem(request.getMenuItemId());
        } catch (Exception e) {
            throw new CustomException("Menu item not found with ID: " + request.getMenuItemId());
        }

        // 2. Check if menu item is available
        if (menuItem.getIsAvailable() == null || !menuItem.getIsAvailable()) {
            throw new CustomException("Menu item is currently unavailable");
        }

        // 3. Get or create cart
        Cart cart = getOrCreateCart(request.getCustomerId());

        // 4. Single-restaurant rule
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(menuItem.getRestaurantId())) {
            throw new CustomException("Cart already contains items from another restaurant. Clear cart or place order first.");
        }

        // 5. Validate restaurant exists (Feign call)
        try {
            restaurantClient.validateRestaurant(menuItem.getRestaurantId());
        } catch (Exception e) {
            throw new CustomException("Restaurant not found with ID: " + menuItem.getRestaurantId());
        }

        // 6. Set restaurant on cart if first item
        if (cart.getRestaurantId() == null) {
            cart.setRestaurantId(menuItem.getRestaurantId());
            cartRepository.save(cart);
        }

        // 7. Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCart_CartIdAndMenuItemId(cart.getCartId(), request.getMenuItemId())
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            log.info("Updated quantity of existing item in cart");
        } else {
            // Create new cart item (snapshot)
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setMenuItemId(menuItem.getItemId());
            newItem.setName(menuItem.getName());
            newItem.setPrice(menuItem.getDiscountedPrice() != null && menuItem.getDiscountedPrice() > 0 
                            ? menuItem.getDiscountedPrice() : menuItem.getPrice());
            newItem.setQuantity(request.getQuantity());
            newItem.setCustomization(request.getCustomization());
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
            log.info("Added new item to cart");
        }

        // 8. Recalculate total
        recalculateTotal(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(UpdateQuantityRequest request) {
        log.info("Updating quantity for customer {} item {}", request.getCustomerId(), request.getMenuItemId());

        Cart cart = cartRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomException("Cart not found for customer"));

        CartItem item = cartItemRepository.findByCart_CartIdAndMenuItemId(cart.getCartId(), request.getMenuItemId())
                .orElseThrow(() -> new CustomException("Item not found in cart"));

        if (request.getQuantity() <= 0) {
            // Remove item if quantity <= 0
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            log.info("Removed item from cart because quantity <= 0");
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        // If cart becomes empty, reset restaurantId
        if (cart.getItems().isEmpty()) {
            cart.setRestaurantId(null);
            cartRepository.save(cart);
        }

        recalculateTotal(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, Long menuItemId) {
        log.info("Removing item {} from cart of customer {}", menuItemId, customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomException("Cart not found for customer"));

        CartItem item = cartItemRepository.findByCart_CartIdAndMenuItemId(cart.getCartId(), menuItemId)
                .orElseThrow(() -> new CustomException("Item not found in cart"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        if (cart.getItems().isEmpty()) {
            cart.setRestaurantId(null);
            cartRepository.save(cart);
        }

        recalculateTotal(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long customerId) {
        log.info("Clearing cart for customer {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomException("Cart not found for customer"));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setRestaurantId(null);
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse applyPromoCode(ApplyPromoRequest request) {
        log.info("Applying promo code {} for customer {}", request.getPromoCode(), request.getCustomerId());

        Cart cart = cartRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomException("Cart not found for customer"));

        if (cart.getItems().isEmpty()) {
            throw new CustomException("Cart is empty, cannot apply promo code");
        }

        // Simple promo validation (10% off for code "SAVE10")
        double discount = PromoValidator.validate(request.getPromoCode(), cart.getTotalPrice());
        if (discount == 0) {
            throw new CustomException("Invalid or expired promo code");
        }

        double newTotal = cart.getTotalPrice() - discount;
        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        log.info("Promo applied: {} discount, new total {}", discount, newTotal);
        return toResponse(cart);
    }

    @Override
    public Double calculateTotal(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomException("Cart not found for customer"));
        return cart.getTotalPrice();
    }
}
