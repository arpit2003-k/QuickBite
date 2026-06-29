package com.quickbite.cart.util;

public class PromoValidator {

    public static double validate(String promoCode, double currentTotal) {
        // Simple hardcoded promos (can be extended with database later)
        switch (promoCode.toUpperCase()) {
            case "SAVE10":
                return currentTotal * 0.10;   // 10% off
            case "FLAT50":
                return 50.0;                  // ₹50 off
            case "WELCOME":
                return 100.0;                 // ₹100 off on first order
            default:
                return 0.0;                   // invalid
        }
    }
}
