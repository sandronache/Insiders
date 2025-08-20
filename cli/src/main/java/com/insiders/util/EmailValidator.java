package com.insiders.util;

import java.util.regex.Pattern;

public class EmailValidator {
    private static final String EMAIL_PATTERN =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        email = email.trim();

        if (email.length() > 254) {
            return false;
        }

        return pattern.matcher(email).matches();
    }

    public static String getEmailErrorMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty!";
        }

        email = email.trim();

        if (email.length() > 254) {
            return "Email is too long! Maximum length is 254 characters.";
        }

        if (!email.contains("@")) {
            return "Email must contain an @ symbol!";
        }

        if (email.startsWith("@") || email.endsWith("@")) {
            return "Email cannot start or end with @!";
        }

        if (!email.contains(".")) {
            return "Email must contain a domain with a dot (e.g., .com, .org)!";
        }

        return "Invalid email format! Please enter a valid email address (e.g., user@example.com).";
    }
}
