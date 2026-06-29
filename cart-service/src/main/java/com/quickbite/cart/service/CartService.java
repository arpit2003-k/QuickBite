package com.quickbite.cart.service;

import com.quickbite.cart.dto.*;

public interface CartService {
    CartResponse getCart(Long customerId);
    CartResponse addItem(AddItemRequest request);
    CartResponse updateQuantity(UpdateQuantityRequest request);
    CartResponse removeItem(Long customerId, Long menuItemId);
    CartResponse clearCart(Long customerId);
    CartResponse applyPromoCode(ApplyPromoRequest request);
    Double calculateTotal(Long customerId);
}
