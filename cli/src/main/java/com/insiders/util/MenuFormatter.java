package com.insiders.util;

public class MenuFormatter {
    public static final String HORIZONTAL_LINE = "═";
    public static final String VERTICAL_LINE = "║";
    public static final String TOP_LEFT = "╔";
    public static final String TOP_RIGHT = "╗";
    public static final String BOTTOM_LEFT = "╚";
    public static final String BOTTOM_RIGHT = "╝";
    private static final String CROSS = "╬";
    private static final String TOP_CROSS = "╦";
    private static final String BOTTOM_CROSS = "╩";
    private static final String LEFT_CROSS = "╠";
    private static final String RIGHT_CROSS = "╣";

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";


    public static void printWelcomeHeader(String username) {
        int width = 60;
        String welcomeText = "Welcome " + username + "!";
        String subtitle = "Latest Posts";

        System.out.println();
        printBoxedHeader(welcomeText, subtitle, width, CYAN, BOLD);
        System.out.println();
    }

    public static void printMenuHeader(String title) {
        int width = 50;
        System.out.println();
        printBoxedHeader(title, null, width, BLUE, BOLD);
    }

    public static void printMenuOptions(String... options) {
        System.out.println();
        for (String option : options) {
            if (option.startsWith("0.")) {
                System.out.println(RED + "  " + option + RESET);
            } else {
                System.out.println(GREEN + "  " + option + RESET);
            }
        }
        System.out.println();
        printSeparator(50, "─");
    }

    public static void printPostHeader() {
        System.out.println();
        printBoxedHeader("Posts Feed", null, 80, PURPLE, BOLD);
        System.out.println();
    }

    public static void printPostCard(int id, String title, String author, boolean isOwnPost,
                                   String subreddit, int score, int commentCount, String timeAgo, String imageUrl) {
        int width = 80;
        System.out.println(TOP_LEFT + HORIZONTAL_LINE.repeat(width - 2) + TOP_RIGHT);

        String idLine = String.format("ID: %s%d%s", YELLOW + BOLD, id, RESET);
        String titleLine = String.format("Title: %s%s%s", CYAN + BOLD, title, RESET);
        printBoxLine(idLine, width);
        printBoxLine(titleLine, width);

        String authorDisplay = isOwnPost ?
            String.format("%s%s%s %s📝[YOUR POST]%s", GREEN + BOLD, author, RESET, YELLOW, RESET) :
            String.format("%s%s%s", GREEN, author, RESET);
        String authorLine = "Author: " + authorDisplay;
        printBoxLine(authorLine, width);

        String subredditLine = String.format("Subreddit: %s%s%s", BLUE, subreddit, RESET);
        String scoreColor = score >= 0 ? GREEN : RED;
        String scoreLine = String.format("Score: %s%d%s", scoreColor, score, RESET);
        printBoxLine(subredditLine, width);
        printBoxLine(scoreLine, width);

        String commentsLine = String.format("Comments: %s%d%s", CYAN, commentCount, RESET);
        printBoxLine(commentsLine, width);

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String filename = extractFilename(imageUrl);
            String imageLine = String.format("Image: %s📷 %s%s", PURPLE, filename, RESET);
            printBoxLine(imageLine, width);
        }

        String timeLine = String.format("Posted: %s%s%s", PURPLE, timeAgo, RESET);
        printBoxLine(timeLine, width);

        System.out.println(BOTTOM_LEFT + HORIZONTAL_LINE.repeat(width - 2) + BOTTOM_RIGHT);
        System.out.println();
    }

    // Overloaded method for backward compatibility
    public static void printPostCard(int id, String title, String author, boolean isOwnPost,
                                   String subreddit, int score, int commentCount, String timeAgo) {
        printPostCard(id, title, author, isOwnPost, subreddit, score, commentCount, timeAgo, null);
    }

    public static void printCreatePostHeader() {
        System.out.println();
        printBoxedHeader("Create New Post", null, 60, GREEN, BOLD);
        System.out.println();
    }

    public static void printSuccessMessage(String message) {
        System.out.println();
        System.out.println(GREEN + BOLD + "✓ " + message + RESET);
        System.out.println();
    }

    public static void printErrorMessage(String message) {
        System.out.println();
        System.out.println(RED + BOLD + "✗ " + message + RESET);
        System.out.println();
    }

    public static void printInfoMessage(String message) {
        System.out.println();
        System.out.println(CYAN + "ℹ " + message + RESET);
        System.out.println();
    }

    public static void printWarningMessage(String message) {
        System.out.println();
        System.out.println(YELLOW + "⚠ " + message + RESET);
        System.out.println();
    }

    public static void printPostDetails(String title, String content, String author, boolean isOwnPost,
                                      String subreddit, int upvotes, int downvotes, String userVote, String timeAgo, String imageUrl) {
        int width = 80;

        System.out.println(TOP_LEFT + HORIZONTAL_LINE.repeat(width - 2) + TOP_RIGHT);

        String titleLine = String.format("Title: %s%s%s", CYAN + BOLD, title, RESET);
        printBoxLine(titleLine, width);

        String contentLine = String.format("Content: %s%s%s", WHITE, content != null ? content : "", RESET);
        printBoxLine(contentLine, width);

        String authorDisplay = isOwnPost ?
            String.format("%s%s%s %s📝 [YOUR POST]%s", GREEN + BOLD, author, RESET, YELLOW, RESET) :
            String.format("%s%s%s", GREEN, author, RESET);
        String authorLine = "Author: " + authorDisplay;
        printBoxLine(authorLine, width);

        String subredditLine = String.format("Subreddit: %s%s%s", BLUE, subreddit, RESET);
        printBoxLine(subredditLine, width);

        // Add image filename if present
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String filename = extractFilename(imageUrl);
            String imageLine = String.format("Image: %s📷 %s%s", PURPLE, filename, RESET);
            printBoxLine(imageLine, width);
        }

        int score = upvotes - downvotes;
        String scoreColor = score >= 0 ? GREEN : RED;
        String scoreLine = String.format("Score: %s%d%s", scoreColor, score, RESET);
        printBoxLine(scoreLine, width);

        String votesLine = String.format("Votes: %s%d%s↑ %s%d%s↓",
            GREEN, upvotes, RESET, RED, downvotes, RESET);
        printBoxLine(votesLine, width);

        if (userVote != null && !userVote.isEmpty() && !"none".equals(userVote)) {
            String voteDisplay = userVote.equals("up") ?
                String.format("%s⬆️YOU UPVOTED%s", GREEN + BOLD, RESET) :
                String.format("%s⬇️YOU DOWNVOTED%s", RED + BOLD, RESET);
            String userVoteLine = "Your vote: " + voteDisplay;
            printBoxLine(userVoteLine, width);
        }

        String timeLine = String.format("Posted: %s%s%s", PURPLE, timeAgo, RESET);
        printBoxLine(timeLine, width);

        System.out.println(BOTTOM_LEFT + HORIZONTAL_LINE.repeat(width - 2) + BOTTOM_RIGHT);
        printEmptyLine();
    }

    // Overloaded method for backward compatibility
    public static void printPostDetails(String title, String content, String author, boolean isOwnPost,
                                      String subreddit, int upvotes, int downvotes, String userVote, String timeAgo) {
        printPostDetails(title, content, author, isOwnPost, subreddit, upvotes, downvotes, userVote, timeAgo, null);
    }

    public static void printCommentsHeader(int commentCount) {
        System.out.println();
        String title = commentCount > 0 ?
            "All Comments (" + commentCount + ")" :
            "No Comments";
        printBoxedHeader(title, null, 80, PURPLE, BOLD);
        System.out.println();
    }

    public static void printCommentCard(int id, String author, boolean isOwnComment,
                                      String content, int score, int upvotes, int downvotes,
                                      String userVote, String timeAgo, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        String replyIndicator = indentLevel > 0 ? "↳ " : "";
        int width = 80 - (indentLevel * 2);

        System.out.println(indent + TOP_LEFT + HORIZONTAL_LINE.repeat(width - 2) + TOP_RIGHT);

        String idLine = replyIndicator + String.format("Comment ID: %s%d%s", YELLOW + BOLD, id, RESET);
        printCommentLine(idLine, width, indent);

        String authorDisplay = isOwnComment ?
            String.format("%s%s%s %s💬[YOUR COMMENT]%s", GREEN + BOLD, author, RESET, YELLOW, RESET) :
            String.format("%s%s%s", GREEN, author, RESET);
        String authorLine = "Author: " + authorDisplay;
        printCommentLine(authorLine, width, indent);

        String contentLine = String.format("Content: %s%s%s", WHITE, content, RESET);
        printCommentLine(contentLine, width, indent);

        String scoreColor = score >= 0 ? GREEN : RED;
        String scoreLine = String.format("Score: %s%d%s (%s%d%s↑ %s%d%s↓)",
            scoreColor, score, RESET,
            GREEN, upvotes, RESET,
            RED, downvotes, RESET);

        if (userVote != null && !userVote.isEmpty() && !"none".equals(userVote)) {
            String voteDisplay = userVote.equals("up") ?
                String.format(" %s⬆️[YOU UPVOTED]%s", GREEN + BOLD, RESET) :
                String.format(" %s⬇️[YOU DOWNVOTED]%s", RED + BOLD, RESET);
            scoreLine += voteDisplay;
        }
        printCommentLine(scoreLine, width, indent);

        String timeLine = String.format("Posted: %s%s%s", PURPLE, timeAgo, RESET);
        printCommentLine(timeLine, width, indent);
        System.out.println(indent + BOTTOM_LEFT + HORIZONTAL_LINE.repeat(width - 2) + BOTTOM_RIGHT);
        System.out.println();
    }

    private static void printBoxedHeader(String title, String subtitle, int width, String color, String style) {
        System.out.println(color + style + TOP_LEFT + HORIZONTAL_LINE.repeat(width - 2) + TOP_RIGHT + RESET);

        String titleLine = centerText(title, width - 4);
        System.out.println(color + style + VERTICAL_LINE + " " + titleLine + " " + VERTICAL_LINE + RESET);

        if (subtitle != null && !subtitle.isEmpty()) {
            String subtitleLine = centerText(subtitle, width - 4);
            System.out.println(color + style + VERTICAL_LINE + " " + subtitleLine + " " + VERTICAL_LINE + RESET);
        }

        System.out.println(color + style + BOTTOM_LEFT + HORIZONTAL_LINE.repeat(width - 2) + BOTTOM_RIGHT + RESET);
    }

    private static void printBoxLine(String text, int width) {
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        String padding = " ".repeat(Math.max(0, width - 4 - cleanText.length()));
        System.out.println(VERTICAL_LINE + " " + text + padding + " " + VERTICAL_LINE);
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        String leftPad = " ".repeat(padding);
        String rightPad = " ".repeat(width - text.length() - padding);
        return leftPad + text + rightPad;
    }

    public static void printSeparator(int width, String character) {
        System.out.println(character.repeat(width));
    }

    public static void printEmptyLine() {
        System.out.println();
    }

    private static void printCommentLine(String text, int width, String indent) {
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        String padding = " ".repeat(Math.max(0, width - 4 - cleanText.length()));
        System.out.println(indent + VERTICAL_LINE + " " + text + padding + " " + VERTICAL_LINE);
    }

    public static void printCommentActionsMenu() {
        printMenuHeader("Comment Actions");
        printMenuOptions(
            "1. Add comment",
            "2. Reply to existing comment",
            "3. Edit comment",
            "4. Delete comment",
            "5. Upvote comment",
            "6. Downvote comment",
            "0. Back"
        );
    }

    public static String readPasswordWithMasking(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return pwd == null ? "" : new String(pwd);
        } else {
            printWarningMessage("Running in IDE - password will be visible while typing");
            printInfoMessage("For secure password input, run: java -jar target/cli-1.0-SNAPSHOT.jar");
            System.out.print(prompt);
            return new java.util.Scanner(System.in).nextLine();
        }
    }

    private static String extractFilename(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "unknown";
        }
        int lastSlash = imageUrl.lastIndexOf('/');
        String filename;
        if (lastSlash >= 0 && lastSlash < imageUrl.length() - 1) {
            filename = imageUrl.substring(lastSlash + 1);
        } else {
            filename = imageUrl;
        }

        if (filename.contains("_")) {
            int underscoreIndex = filename.indexOf('_');
            String beforeUnderscore = filename.substring(0, underscoreIndex);

            if (beforeUnderscore.length() == 36 && beforeUnderscore.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                filename = filename.substring(underscoreIndex + 1);
            }
        }

        return filename;
    }
}
