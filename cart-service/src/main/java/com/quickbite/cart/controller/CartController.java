package com.quickbite.cart.controller;

import com.quickbite.cart.dto.*;
import com.quickbite.cart.service.CartService;
import com.quickbite.cart.util.RoleValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Slf4j
@Tag(name = "Cart", description = "Manage shopping cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{customerId}")
    @Operation(summary = "Get cart by customer ID")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long customerId,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Skip auth if called internally (no headers = trusted service-to-service call)
        boolean isInternalCall = (userId == null && userRole == null);
        if (!isInternalCall && !"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddItemRequest request,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !"CUSTOMER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new com.quickbite.cart.exception.CustomException("Only customers can add items to cart");
        }
        if (userId != null) request.setCustomerId(userId);
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<CartResponse> updateQuantity(@Valid @RequestBody UpdateQuantityRequest request,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                       @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !"CUSTOMER".equals(userRole) && !"ADMIN".equals(userRole)) {
            throw new com.quickbite.cart.exception.CustomException("Only customers can update cart quantities");
        }
        if (userId != null) request.setCustomerId(userId);
        return ResponseEntity.ok(cartService.updateQuantity(request));
    }

    @DeleteMapping("/items")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(@RequestParam Long customerId, 
                                                   @RequestParam Long menuItemId,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                   @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(cartService.removeItem(customerId, menuItemId));
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<CartResponse> clearCart(@PathVariable Long customerId,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                  @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Skip auth if called internally (no headers = trusted service-to-service call)
        boolean isInternalCall = (userId == null && userRole == null);
        if (!isInternalCall && !"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(cartService.clearCart(customerId));
    }

    @PostMapping("/promo")
    @Operation(summary = "Apply promo code")
    public ResponseEntity<CartResponse> applyPromo(@Valid @RequestBody ApplyPromoRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                   @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userRole != null && !"CUSTOMER".equals(userRole)) {
            throw new com.quickbite.cart.exception.CustomException("Only customers can apply promo codes");
        }
        if (userId != null) 
        	request.setCustomerId(userId);
        return ResponseEntity.ok(cartService.applyPromoCode(request));
    }

    @GetMapping("/total/{customerId}")
    @Operation(summary = "Get cart total")
    public ResponseEntity<Double> getTotal(@PathVariable Long customerId,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (!"ADMIN".equals(userRole)) {
            RoleValidator.checkUserId(customerId, userId);
        }
        return ResponseEntity.ok(cartService.calculateTotal(customerId));
    }
}
