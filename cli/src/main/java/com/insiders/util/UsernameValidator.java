package com.insiders.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class UsernameValidator {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern STARTS_WITH_LETTER = Pattern.compile("^[a-zA-Z].*");

    // Reserved usernames that shouldn't be allowed
    private static final Set<String> RESERVED_USERNAMES = new HashSet<>(Arrays.asList(
        "admin", "administrator", "root", "user", "guest", "anonymous", "system", "null",
        "undefined", "test", "demo", "example", "api", "www", "mail", "ftp", "support",
        "moderator", "mod", "insiders", "reddit", "subreddit"
    ));

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        username = username.trim();

        // Check length
        if (username.length() < MIN_LENGTH || username.length() > MAX_LENGTH) {
            return false;
        }

        // Check pattern (alfanumeric și underscore only)
        if (!VALID_PATTERN.matcher(username).matches()) {
            return false;
        }

        // Must start with a letter
        if (!STARTS_WITH_LETTER.matcher(username).matches()) {
            return false;
        }

        // Check if reserved
        if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
            return false;
        }

        // Cannot be all numbers
        if (username.matches("^[0-9_]+$")) {
            return false;
        }

        return true;
    }

    public static String getUsernameErrorMessage(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty!";
        }

        username = username.trim();

        if (username.length() < MIN_LENGTH) {
            return "Username must be at least " + MIN_LENGTH + " characters long!";
        }

        if (username.length() > MAX_LENGTH) {
            return "Username cannot exceed " + MAX_LENGTH + " characters!";
        }

        if (!STARTS_WITH_LETTER.matcher(username).matches()) {
            return "Username must start with a letter!";
        }

        if (!VALID_PATTERN.matcher(username).matches()) {
            return "Username can only contain letters, numbers, and underscores!";
        }

        if (username.matches("^[0-9_]+$")) {
            return "Username cannot be only numbers and underscores!";
        }

        if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
            return "This username is reserved and cannot be used!";
        }

        return "Invalid username format!";
    }
}
