package com.insiders.util;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|script)");

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }

        // Trim whitespace
        input = input.trim();

        // Normalize multiple whitespaces to single space
        input = WHITESPACE_PATTERN.matcher(input).replaceAll(" ");

        // Remove potential HTML/XML tags
        input = SPECIAL_CHARS_PATTERN.matcher(input).replaceAll("");

        return input;
    }

    public static boolean isSafeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true; // Empty input is safe
        }

        String lowerInput = input.toLowerCase();

        // Check for SQL injection attempts
        if (SQL_INJECTION_PATTERN.matcher(lowerInput).find()) {
            return false;
        }

        // Check for script tags
        if (lowerInput.contains("<script") || lowerInput.contains("javascript:")) {
            return false;
        }

        return true;
    }

    public static boolean isValidNumberInRange(String input, int min, int max) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            int value = Integer.parseInt(input.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
