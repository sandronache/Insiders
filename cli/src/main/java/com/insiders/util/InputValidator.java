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

    public static boolean isPrintableAscii(String input) {
        if (input == null) {
            return true;
        }

        for (char c : input.toCharArray()) {
            if (c < 32 || c > 126) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWithinLength(String input, int maxLength) {
        return input == null || input.length() <= maxLength;
    }

    public static boolean meetsMinLength(String input, int minLength) {
        return input != null && input.trim().length() >= minLength;
    }

    public static String getInputSafetyError(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Input cannot be empty!";
        }

        String lowerInput = input.toLowerCase();

        if (SQL_INJECTION_PATTERN.matcher(lowerInput).find()) {
            return "Input contains potentially dangerous SQL keywords!";
        }

        if (lowerInput.contains("<script") || lowerInput.contains("javascript:")) {
            return "Input contains potentially dangerous script content!";
        }

        if (!isPrintableAscii(input)) {
            return "Input contains non-printable or non-ASCII characters!";
        }

        return "Input contains unsafe content!";
    }

    public static ValidationResult validateInput(String input, int minLength, int maxLength) {
        if (input == null) {
            return new ValidationResult(false, "Input cannot be null!");
        }

        String trimmed = input.trim();

        if (trimmed.isEmpty()) {
            return new ValidationResult(false, "Input cannot be empty!");
        }

        if (!meetsMinLength(trimmed, minLength)) {
            return new ValidationResult(false, "Input must be at least " + minLength + " characters long!");
        }

        if (!isWithinLength(trimmed, maxLength)) {
            return new ValidationResult(false, "Input cannot exceed " + maxLength + " characters!");
        }

        if (!isSafeInput(trimmed)) {
            return new ValidationResult(false, getInputSafetyError(trimmed));
        }

        if (!isPrintableAscii(trimmed)) {
            return new ValidationResult(false, "Input contains invalid characters!");
        }

        return new ValidationResult(true, "Input is valid");
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
