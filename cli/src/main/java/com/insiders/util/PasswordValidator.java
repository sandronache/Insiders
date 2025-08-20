package com.insiders.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private static final Pattern HAS_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public enum PasswordStrength {
        VERY_WEAK("Very Weak", 0),
        WEAK("Weak", 1),
        FAIR("Fair", 2),
        GOOD("Good", 3),
        STRONG("Strong", 4),
        VERY_STRONG("Very Strong", 5);

        private final String displayName;
        private final int score;

        PasswordStrength(String displayName, int score) {
            this.displayName = displayName;
            this.score = score;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getScore() {
            return score;
        }
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Check basic length requirement
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }

        // Must have at least 2 of the 4 character types for minimum security
        int typeCount = 0;
        if (HAS_LOWERCASE.matcher(password).matches()) typeCount++;
        if (HAS_UPPERCASE.matcher(password).matches()) typeCount++;
        if (HAS_DIGIT.matcher(password).matches()) typeCount++;
        if (HAS_SPECIAL.matcher(password).matches()) typeCount++;

        return typeCount >= 2;
    }

    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.VERY_WEAK;
        }

        int score = 0;

        // Length scoring
        if (password.length() >= MIN_LENGTH) score++;
        if (password.length() >= 12) score++;

        // Character type scoring
        if (HAS_LOWERCASE.matcher(password).matches()) score++;
        if (HAS_UPPERCASE.matcher(password).matches()) score++;
        if (HAS_DIGIT.matcher(password).matches()) score++;
        if (HAS_SPECIAL.matcher(password).matches()) score++;

        // Bonus for longer passwords
        if (password.length() >= 16) score++;

        // Cap the score
        score = Math.min(score, 5);

        return switch (score) {
            case 0, 1 -> PasswordStrength.VERY_WEAK;
            case 2 -> PasswordStrength.WEAK;
            case 3 -> PasswordStrength.FAIR;
            case 4 -> PasswordStrength.GOOD;
            case 5 -> PasswordStrength.STRONG;
            default -> PasswordStrength.VERY_WEAK;
        };
    }

    public static String getPasswordErrorMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty!";
        }

        if (password.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters long!";
        }

        if (password.length() > MAX_LENGTH) {
            return "Password cannot exceed " + MAX_LENGTH + " characters!";
        }

        int typeCount = 0;
        if (HAS_LOWERCASE.matcher(password).matches()) typeCount++;
        if (HAS_UPPERCASE.matcher(password).matches()) typeCount++;
        if (HAS_DIGIT.matcher(password).matches()) typeCount++;
        if (HAS_SPECIAL.matcher(password).matches()) typeCount++;

        if (typeCount < 2) {
            return "Password must contain at least 2 of: lowercase letters, uppercase letters, numbers, or special characters!";
        }

        return "Invalid password!";
    }

    public static String getPasswordStrengthTips(String password) {
        StringBuilder tips = new StringBuilder();

        if (password.length() < 12) {
            tips.append("• Use at least 12 characters for better security\n");
        }

        if (!HAS_LOWERCASE.matcher(password).matches()) {
            tips.append("• Add lowercase letters (a-z)\n");
        }

        if (!HAS_UPPERCASE.matcher(password).matches()) {
            tips.append("• Add uppercase letters (A-Z)\n");
        }

        if (!HAS_DIGIT.matcher(password).matches()) {
            tips.append("• Add numbers (0-9)\n");
        }

        if (!HAS_SPECIAL.matcher(password).matches()) {
            tips.append("• Add special characters (!@#$%^&*)\n");
        }

        return tips.toString();
    }
}
