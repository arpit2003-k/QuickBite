package com.quickbite.cart.repository;

import com.quickbite.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_CartIdAndMenuItemId(Long cartId, Long menuItemId);
}
