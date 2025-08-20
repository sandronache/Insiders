package com.insiders.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ContentValidator {
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 300;
    private static final int MIN_CONTENT_LENGTH = 1;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MIN_COMMENT_LENGTH = 1;
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int MIN_SUBREDDIT_NAME_LENGTH = 3;
    private static final int MAX_SUBREDDIT_NAME_LENGTH = 50;

    // Subreddit name pattern (alphanumeric și underscore)
    private static final Pattern SUBREDDIT_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    // Basic profanity filter
    private static final Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
        "spam", "scam", "fake", "hate", "abuse", "harassment", "troll"
    ));

    // URL pattern for HTTP/HTTPS validation
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$"
    );

    public static boolean isValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        title = title.trim();
        return title.length() >= MIN_TITLE_LENGTH &&
               title.length() <= MAX_TITLE_LENGTH &&
               !containsProfanity(title);
    }

    public static boolean isValidContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        content = content.trim();
        return content.length() >= MIN_CONTENT_LENGTH &&
               content.length() <= MAX_CONTENT_LENGTH &&
               !containsProfanity(content);
    }

    public static boolean isValidComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        comment = comment.trim();
        return comment.length() >= MIN_COMMENT_LENGTH &&
               comment.length() <= MAX_COMMENT_LENGTH &&
               !containsProfanity(comment);
    }

    public static boolean isValidSubredditName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        name = name.trim();

        // Check length
        if (name.length() < MIN_SUBREDDIT_NAME_LENGTH || name.length() > MAX_SUBREDDIT_NAME_LENGTH) {
            return false;
        }

        // Check pattern (alfanumeric și underscore)
        if (!SUBREDDIT_PATTERN.matcher(name).matches()) {
            return false;
        }

        // Cannot start with underscore
        if (name.startsWith("_")) {
            return false;
        }

        // Check profanity
        if (containsProfanity(name)) {
            return false;
        }

        return true;
    }

    public static boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true;
        }

        url = url.trim();

        if (!isValidUrl(url)) {
            return false;
        }

        // Then check if it's specifically an image URL
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") ||
               lowerUrl.endsWith(".jpeg") ||
               lowerUrl.endsWith(".png") ||
               lowerUrl.endsWith(".gif") ||
               lowerUrl.endsWith(".webp") ||
               lowerUrl.contains("imgur.com") ||
               lowerUrl.contains("reddit.com") ||
               lowerUrl.contains("i.redd.it");
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        return URL_PATTERN.matcher(url.trim()).matches();
    }

    private static boolean containsProfanity(String text) {
        if (text == null) return false;

        String lowerText = text.toLowerCase();
        return PROFANITY_WORDS.stream().anyMatch(lowerText::contains);
    }

    public static String getTitleErrorMessage(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Title cannot be empty!";
        }

        title = title.trim();

        if (title.length() < MIN_TITLE_LENGTH) {
            return "Title must be at least " + MIN_TITLE_LENGTH + " characters long!";
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            return "Title cannot exceed " + MAX_TITLE_LENGTH + " characters!";
        }

        if (containsProfanity(title)) {
            return "Title contains inappropriate content!";
        }

        return "Invalid title!";
    }

    public static String getContentErrorMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Content cannot be empty!";
        }

        content = content.trim();

        if (content.isEmpty()) {
            return "Content cannot be empty!";
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            return "Content cannot exceed " + MAX_CONTENT_LENGTH + " characters!";
        }

        if (containsProfanity(content)) {
            return "Content contains inappropriate language!";
        }

        return "Invalid content!";
    }

    public static String getCommentErrorMessage(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return "Comment cannot be empty!";
        }

        comment = comment.trim();

        if (comment.isEmpty()) {
            return "Comment cannot be empty!";
        }

        if (comment.length() > MAX_COMMENT_LENGTH) {
            return "Comment cannot exceed " + MAX_COMMENT_LENGTH + " characters!";
        }

        if (containsProfanity(comment)) {
            return "Comment contains inappropriate language!";
        }

        return "Invalid comment!";
    }

    public static String getSubredditNameErrorMessage(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Subreddit name cannot be empty!";
        }

        name = name.trim();

        if (name.length() < MIN_SUBREDDIT_NAME_LENGTH) {
            return "Subreddit name must be at least " + MIN_SUBREDDIT_NAME_LENGTH + " characters long!";
        }

        if (name.length() > MAX_SUBREDDIT_NAME_LENGTH) {
            return "Subreddit name cannot exceed " + MAX_SUBREDDIT_NAME_LENGTH + " characters!";
        }

        if (name.startsWith("_")) {
            return "Subreddit name cannot start with underscore!";
        }

        if (!SUBREDDIT_PATTERN.matcher(name).matches()) {
            return "Subreddit name can only contain letters, numbers, and underscores!";
        }

        if (containsProfanity(name)) {
            return "Subreddit name contains inappropriate content!";
        }

        return "Invalid subreddit name!";
    }

    public static String getImageUrlErrorMessage(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "Image URL cannot be empty!";
        }

        url = url.trim();

        if (!URL_PATTERN.matcher(url).matches()) {
            return "Invalid HTTP/HTTPS URL format!";
        }

        String lowerUrl = url.toLowerCase();
        if (!(lowerUrl.endsWith(".jpg") ||
              lowerUrl.endsWith(".jpeg") ||
              lowerUrl.endsWith(".png") ||
              lowerUrl.endsWith(".gif") ||
              lowerUrl.endsWith(".webp") ||
              lowerUrl.contains("imgur.com") ||
              lowerUrl.contains("reddit.com") ||
              lowerUrl.contains("i.redd.it"))) {
            return "URL must point to a valid image format (.jpg, .png, .gif, .webp) or supported image host!";
        }

        return "Invalid image URL!";
    }
}
