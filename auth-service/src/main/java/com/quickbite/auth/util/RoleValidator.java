package com.quickbite.auth.util;

import com.quickbite.auth.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RoleValidator {

    public static boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public static boolean isRestaurantOwner() {
        return hasRole("RESTAURANT_OWNER");
    }

    public static boolean isDeliveryAgent() {
        return hasRole("DELIVERY_AGENT");
    }
}