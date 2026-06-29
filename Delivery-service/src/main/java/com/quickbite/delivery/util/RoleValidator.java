package com.quickbite.delivery.util;

import com.quickbite.delivery.exception.CustomException;

public class RoleValidator {

    public static void checkRole(String requiredRole, String userRole) {
        if (userRole == null || !userRole.equals(requiredRole)) {
            throw new CustomException("Access denied. Required role: " + requiredRole);
        }
    }

    public static void checkUserId(Long expectedUserId, Long userIdFromHeader) {
        if (expectedUserId == null || userIdFromHeader == null || !expectedUserId.equals(userIdFromHeader)) {
            throw new CustomException("You are not authorized to access this resource");
        }
    }
}
