package com.inventory.util;

public class ValidationUtil {
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isPositiveNumber(String text) {
        if (isBlank(text)) {
            return false;
        }
        try {
            double value = Double.parseDouble(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidQuantity(String text) {
        if (isBlank(text)) {
            return false;
        }
        try {
            int value = Integer.parseInt(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
