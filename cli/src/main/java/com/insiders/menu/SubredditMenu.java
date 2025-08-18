package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.clients.SubredditClient;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.subreddit.SubredditCreateRequestDto;
import com.insiders.dto.subreddit.SubredditResponseDto;
import com.insiders.dto.subreddit.SubredditUpdateRequestDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.TimeUtils;
import com.insiders.util.MenuFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubredditMenu {
    private final SubredditClient subredditClient;
    private final SessionManager sessionManager;
    private final PostMenu postMenu;
    private final Map<Integer, String> subredditNameMapping = new HashMap<>();
    private final Map<Integer, UUID> postIdMapping = new HashMap<>();

    private static final int SUBREDDITS_PER_PAGE = 10;
    private List<SubredditResponseDto> allSubreddits = null;
    private int currentSubredditPage = 0;

    private SubredditSortType currentSubredditSortType = SubredditSortType.ALPHABETIC_ASC;

    public enum SubredditSortType {
        ALPHABETIC_ASC("Alphabetical A-Z"),
        ALPHABETIC_DESC("Alphabetical Z-A"),
        POSTS_DESC("Most Posts"),
        POSTS_ASC("Least Posts");

        private final String displayName;

        SubredditSortType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final int POSTS_PER_PAGE = 10;
    private List<PostResponseDto> allSubredditPosts = null;
    private int currentPostPage = 0;
    private String currentSubredditName = null;

    private PostSortType currentPostSortType = PostSortType.DATE_DESC;

    public enum PostSortType {
        DATE_DESC("Newest First"),
        DATE_ASC("Oldest First"),
        SCORE_DESC("Highest Score"),
        COMMENTS_DESC("Most Comments");

        private final String displayName;

        PostSortType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public SubredditMenu(PostClient postClient, SubredditClient subredditClient, SessionManager sessionManager) {
        this.subredditClient = subredditClient;
        this.sessionManager = sessionManager;
        this.postMenu = new PostMenu(postClient, sessionManager);
    }

    public void showSubredditActions() {
        while (true) {
            MenuFormatter.printMenuHeader("Subreddit Actions");
            MenuFormatter.printMenuOptions(
                "1. Create Subreddit",
                "2. All Subreddits",
                "0. Back"
            );

            int choice = ConsoleIO.readInt("Choose option: ");

            switch (choice) {
                case 1 -> createSubreddit();
                case 2 -> allSubreddits();
                case 0 -> {
                    MenuFormatter.printInfoMessage("Returning to feed menu...");
                    return;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice!");
            }
        }
    }

    private void createSubreddit() {
        MenuFormatter.printMenuHeader("Create New Subreddit");

        String name = ConsoleIO.readLine("Enter subreddit name (lowercase, alphanumeric and underscore only): ");
        if (name.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        String displayName = ConsoleIO.readLine("Enter display name: ");
        if (displayName.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Display name cannot be empty!");
            return;
        }

        String description = ConsoleIO.readLine("Enter subreddit description: ");
        if (description.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Description cannot be empty!");
            return;
        }

        String iconUrl = ConsoleIO.readLine("Enter icon URL (optional, press Enter to skip): ");
        if (iconUrl.trim().isEmpty()) {
            iconUrl = null;
        }

        SubredditCreateRequestDto createRequest = new SubredditCreateRequestDto(
            name.toLowerCase().trim(),
            displayName.trim(),
            description.trim(),
            iconUrl
        );

        MenuFormatter.printInfoMessage("Creating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.createSubreddit(createRequest);

        if (result.success) {
            SubredditResponseDto createdSubreddit = result.data;
            MenuFormatter.printSuccessMessage("Subreddit created successfully!");
            MenuFormatter.printInfoMessage("Name: " + createdSubreddit.name());
            MenuFormatter.printInfoMessage("Display Name: " + createdSubreddit.displayName());
            MenuFormatter.printInfoMessage("Description: " + createdSubreddit.description());
            MenuFormatter.printInfoMessage("You can now create posts in this subreddit!");
        } else {
            MenuFormatter.printErrorMessage("Failed to create subreddit!");

            if (result.status == 400) {
                MenuFormatter.printErrorMessage("Invalid input data:");
                if (result.message != null && !result.message.equals("OK")) {
                    MenuFormatter.printErrorMessage("• " + result.message);
                } else {
                    MenuFormatter.printErrorMessage("• Please check that:");
                    MenuFormatter.printErrorMessage("  - Subreddit name contains only lowercase letters, numbers, and underscores");
                    MenuFormatter.printErrorMessage("  - Subreddit name is not already taken");
                    MenuFormatter.printErrorMessage("  - Display name and description are not empty");
                    MenuFormatter.printErrorMessage("  - Icon URL is valid (if provided)");
                }
            } else if (result.status == 409) {
                MenuFormatter.printErrorMessage("• Subreddit name '" + name.toLowerCase().trim() + "' already exists");
                MenuFormatter.printErrorMessage("• Please choose a different name");
            } else if (result.status == 401) {
                MenuFormatter.printErrorMessage("• Authentication failed");
                MenuFormatter.printErrorMessage("• Please log in again");
            } else if (result.status == 403) {
                MenuFormatter.printErrorMessage("• Permission denied");
                MenuFormatter.printErrorMessage("• You don't have permission to create subreddits");
            } else if (result.status == 500) {
                MenuFormatter.printErrorMessage("• Server error occurred");
                MenuFormatter.printErrorMessage("• Please try again later");
            } else {
                MenuFormatter.printErrorMessage("• Error: " + result.message);
                MenuFormatter.printErrorMessage("• Status code: " + result.status);
            }

            MenuFormatter.printInfoMessage("Tip: Subreddit names must be unique and follow naming rules");
        }
    }

    private void allSubreddits() {
        MenuFormatter.printInfoMessage("Loading all subreddits...");

        ApiResult<List<SubredditResponseDto>> result = subredditClient.getAllSubreddits();

        if (result.success) {
            allSubreddits = result.data;
            currentSubredditPage = 0;
            // Apply current sorting after loading subreddits
            applySubredditSort();

            if (allSubreddits.isEmpty()) {
                MenuFormatter.printInfoMessage("No subreddits found.");
                MenuFormatter.printInfoMessage("Be the first to create a subreddit!");
                return;
            } else {
                showPaginatedSubreddits();
            }
        } else {
            MenuFormatter.printErrorMessage("Error loading subreddits: " + result.message);
            return;
        }
    }

    private void showPaginatedSubreddits() {
        while (true) {
            displaySubredditsList();

            MenuFormatter.printMenuHeader("Subreddit Navigation");

            // Build menu options with sequential numbering
            java.util.List<String> menuOptions = new java.util.ArrayList<>();
            menuOptions.add("1. View posts from subreddit");
            menuOptions.add("2. Edit subreddit");
            menuOptions.add("3. Delete subreddit");
            menuOptions.add("4. Sort Subreddits");

            // Track next available option number
            int nextOptionNumber = 5;
            int previousPageOption = -1;
            int nextPageOption = -1;

            if (currentSubredditPage > 0) {
                previousPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Previous Page");
                nextOptionNumber++;
            }
            if (allSubreddits != null && (currentSubredditPage + 1) * SUBREDDITS_PER_PAGE < allSubreddits.size()) {
                nextPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Next Page");
                nextOptionNumber++;
            }

            menuOptions.add("0. Back to Subreddit Actions");

            MenuFormatter.printMenuOptions(menuOptions.toArray(new String[0]));

            int choice = ConsoleIO.readInt("Choose option: ");
            switch (choice) {
                case 1 -> selectSubredditForPosts();
                case 2 -> editSubreddit();
                case 3 -> deleteSubreddit();
                case 4 -> sortSubreddits();
                default -> {
                    // Handle dynamic pagination options
                    if (choice == previousPageOption && currentSubredditPage > 0) {
                        currentSubredditPage--;
                        MenuFormatter.printInfoMessage("Going to previous page...");
                    } else if (choice == nextPageOption && allSubreddits != null && (currentSubredditPage + 1) * SUBREDDITS_PER_PAGE < allSubreddits.size()) {
                        currentSubredditPage++;
                        MenuFormatter.printInfoMessage("Going to next page...");
                    } else if (choice == 0) {
                        MenuFormatter.printInfoMessage("Returning to subreddit actions...");
                        return;
                    } else {
                        MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
                    }
                }
            }
        }
    }

    private void displaySubredditsList() {
        if (allSubreddits == null || allSubreddits.isEmpty()) {
            MenuFormatter.printInfoMessage("No subreddits available to display.");
            return;
        }

        int start = currentSubredditPage * SUBREDDITS_PER_PAGE;
        int end = Math.min(start + SUBREDDITS_PER_PAGE, allSubreddits.size());

        subredditNameMapping.clear();
        MenuFormatter.printMenuHeader("All Subreddits (" + allSubreddits.size() + ")");

        // Show current sorting method
        MenuFormatter.printInfoMessage("Currently sorted by: " + currentSubredditSortType.getDisplayName());

        for (int i = start; i < end; i++) {
            SubredditResponseDto subreddit = allSubreddits.get(i);
            int simpleId = (i - start) + 1; // Use relative position on current page
            subredditNameMapping.put(simpleId, subreddit.name());

            displaySubredditCard(simpleId, subreddit);
        }

        int totalPages = (int) Math.ceil((double) allSubreddits.size() / SUBREDDITS_PER_PAGE);
        MenuFormatter.printInfoMessage(String.format("Showing page %d of %d (%d total subreddits)",
            currentSubredditPage + 1, totalPages, allSubreddits.size()));
    }

    private void displaySubredditCard(int id, SubredditResponseDto subreddit) {
        int width = 80;
        String timeAgo = TimeUtils.getRelativeTime(subreddit.createdAt().toString());

        System.out.println(MenuFormatter.TOP_LEFT + MenuFormatter.HORIZONTAL_LINE.repeat(width - 2) + MenuFormatter.TOP_RIGHT);

        String idLine = String.format("ID: %s%d%s", MenuFormatter.YELLOW + MenuFormatter.BOLD, id, MenuFormatter.RESET);
        String nameLine = String.format("Name: %sr/%s%s", MenuFormatter.CYAN + MenuFormatter.BOLD, subreddit.name(), MenuFormatter.RESET);
        printSubredditLine(idLine, width);
        printSubredditLine(nameLine, width);

        String displayNameLine = String.format("Display Name: %s%s%s", MenuFormatter.GREEN + MenuFormatter.BOLD, subreddit.displayName(), MenuFormatter.RESET);
        printSubredditLine(displayNameLine, width);

        String descriptionLine = String.format("Description: %s%s%s", MenuFormatter.WHITE, subreddit.description(), MenuFormatter.RESET);
        printSubredditLine(descriptionLine, width);
        String postsLine = String.format("Posts: %s%d%s", MenuFormatter.PURPLE, subreddit.postCount(), MenuFormatter.RESET);
        printSubredditLine(postsLine, width);

        String timeLine = String.format("Created: %s%s%s", MenuFormatter.PURPLE, timeAgo, MenuFormatter.RESET);
        printSubredditLine(timeLine, width);

        System.out.println(MenuFormatter.BOTTOM_LEFT + MenuFormatter.HORIZONTAL_LINE.repeat(width - 2) + MenuFormatter.BOTTOM_RIGHT);
        System.out.println();
    }

    private void printSubredditLine(String text, int width) {
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        String padding = " ".repeat(Math.max(0, width - 4 - cleanText.length()));
        System.out.println(MenuFormatter.VERTICAL_LINE + " " + text + padding + " " + MenuFormatter.VERTICAL_LINE);
    }


    private void selectSubredditForPosts() {
        String input = ConsoleIO.readLine("Enter subreddit name (e.g., 'technology' or 'r/technology'): ");
        if (input.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        String subredditName = input.trim();

        if (subredditName.toLowerCase().startsWith("r/")) {
            subredditName = subredditName.substring(2);
        }

        subredditName = subredditName.toLowerCase();

        viewSubredditPosts(subredditName);
    }

    private void viewSubredditPosts(String subredditName) {
        MenuFormatter.printInfoMessage("Loading posts...");

        ApiResult<List<PostResponseDto>> result = subredditClient.getSubredditPosts(subredditName);

        if (result.success) {
            allSubredditPosts = result.data;
            currentPostPage = 0;
            currentSubredditName = subredditName;
            applyPostSort();

            if (allSubredditPosts.isEmpty()) {
                MenuFormatter.printInfoMessage("No posts found in subreddit 'r/" + subredditName + "'.");
                MenuFormatter.printInfoMessage("Be the first to create a post in this subreddit!");
            } else {
                // Show paginated posts with navigation
                showPaginatedSubredditPosts();
            }
        } else {
            MenuFormatter.printErrorMessage("Error loading posts from subreddit 'r/" + subredditName + "': " + result.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
        }
    }

    private void showPaginatedSubredditPosts() {
        while (true) {
            displaySubredditPostsList();

            MenuFormatter.printMenuHeader("Posts Navigation - r/" + currentSubredditName);

            java.util.List<String> menuOptions = new java.util.ArrayList<>();
            menuOptions.add("1. View/Manage Post");
            menuOptions.add("2. Sort Posts");

            int nextOptionNumber = 3;
            int previousPageOption = -1;
            int nextPageOption = -1;

            if (currentPostPage > 0) {
                previousPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Previous Page");
                nextOptionNumber++;
            }
            if (allSubredditPosts != null && (currentPostPage + 1) * POSTS_PER_PAGE < allSubredditPosts.size()) {
                nextPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Next Page");
                nextOptionNumber++;
            }

            menuOptions.add("0. Back to Subreddit Actions");

            MenuFormatter.printMenuOptions(menuOptions.toArray(new String[0]));

            int choice = ConsoleIO.readInt("Choose option: ");
            switch (choice) {
                case 1 -> selectPostToManage();
                case 2 -> sortSubredditPosts();
                default -> {
                    // Handle dynamic pagination options
                    if (choice == previousPageOption && currentPostPage > 0) {
                        currentPostPage--;
                        MenuFormatter.printInfoMessage("Going to previous page...");
                    } else if (choice == nextPageOption && allSubredditPosts != null && (currentPostPage + 1) * POSTS_PER_PAGE < allSubredditPosts.size()) {
                        currentPostPage++;
                        MenuFormatter.printInfoMessage("Going to next page...");
                    } else if (choice == 0) {
                        MenuFormatter.printInfoMessage("Returning to subreddit actions...");
                        return;
                    } else {
                        MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
                    }
                }
            }
        }
    }

    private void displaySubredditPostsList() {
        if (allSubredditPosts == null || allSubredditPosts.isEmpty()) {
            MenuFormatter.printInfoMessage("No posts available to display.");
            return;
        }

        int start = currentPostPage * POSTS_PER_PAGE;
        int end = Math.min(start + POSTS_PER_PAGE, allSubredditPosts.size());

        postIdMapping.clear();
        MenuFormatter.printMenuHeader("Posts in r/" + currentSubredditName + " (" + allSubredditPosts.size() + " total)");

        MenuFormatter.printInfoMessage("Currently sorted by: " + currentPostSortType.getDisplayName());

        for (int i = start; i < end; i++) {
            PostResponseDto post = allSubredditPosts.get(i);
            int simpleId = (i - start) + 1; // Use relative position on current page
            postIdMapping.put(simpleId, post.id());

            boolean isOwnPost = post.author().equals(sessionManager.username());
            int score = post.upvotes() - post.downvotes();
            String timeAgo = TimeUtils.getRelativeTime(post.createdAt().toString());

            MenuFormatter.printPostCard(
                simpleId,
                post.title(),
                post.author(),
                isOwnPost,
                post.subreddit(),
                score,
                post.commentCount(),
                timeAgo
            );
        }

        int totalPages = (int) Math.ceil((double) allSubredditPosts.size() / POSTS_PER_PAGE);
        MenuFormatter.printInfoMessage(String.format("Showing page %d of %d (%d total posts)",
            currentPostPage + 1, totalPages, allSubredditPosts.size()));
    }

    private void selectPostToManage() {
        String input = ConsoleIO.readLine("Enter post ID to view/manage: ");
        if (input.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Post ID cannot be empty!");
            return;
        }

        try {
            int simpleId = Integer.parseInt(input);
            UUID actualPostId = postIdMapping.get(simpleId);
            if (actualPostId != null) {
                postMenu.showPostManagementMenu(actualPostId);
                MenuFormatter.printInfoMessage("Returning to posts in r/" + currentSubredditName + "...");
            } else {
                MenuFormatter.printErrorMessage("Invalid post ID! Please choose a number from the list above (1-" + Math.min(POSTS_PER_PAGE, postIdMapping.size()) + ").");
            }
        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the post list.");
        }
    }

    private void editSubreddit() {
        String input = ConsoleIO.readLine("Enter the subreddit name to edit (e.g., 'technology' or 'r/technology'): ");
        if (input.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        String subredditName = input.trim();

        if (subredditName.toLowerCase().startsWith("r/")) {
            subredditName = subredditName.substring(2);
        }

        subredditName = subredditName.toLowerCase();

        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName);
        if (!subredditResult.success) {
            MenuFormatter.printErrorMessage("Error loading subreddit 'r/" + subredditName + "': " + subredditResult.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        MenuFormatter.printMenuHeader("Edit Subreddit: r/" + subreddit.name());
        MenuFormatter.printInfoMessage("Current Display Name: " + subreddit.displayName());
        MenuFormatter.printInfoMessage("Current Description: " + subreddit.description());

        String newDisplayName = ConsoleIO.readLine("Enter new display name (or press Enter to keep current): ");
        String newDescription = ConsoleIO.readLine("Enter new description (or press Enter to keep current): ");
        String newIconUrl = ConsoleIO.readLine("Enter new icon URL (or press Enter to keep current): ");

        SubredditUpdateRequestDto updateRequest = new SubredditUpdateRequestDto(
            newDisplayName.isEmpty() ? null : newDisplayName,
            newDescription.isEmpty() ? null : newDescription,
            newIconUrl.isEmpty() ? null : newIconUrl
        );

        MenuFormatter.printInfoMessage("Updating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.updateSubreddit(subredditName, updateRequest);

        if (result.success) {
            MenuFormatter.printSuccessMessage("Subreddit updated successfully!");
            MenuFormatter.printInfoMessage("Updated Display Name: " + result.data.displayName());
            MenuFormatter.printInfoMessage("Updated Description: " + result.data.description());
        } else {
            // Enhanced error handling for update operation
            MenuFormatter.printErrorMessage("Failed to update subreddit!");

            if (result.status == 400) {
                MenuFormatter.printErrorMessage("Invalid input data:");
                if (result.message != null && !result.message.equals("OK")) {
                    MenuFormatter.printErrorMessage("• " + result.message);
                } else {
                    MenuFormatter.printErrorMessage("• Please check that the new values are valid");
                }
            } else if (result.status == 404) {
                MenuFormatter.printErrorMessage("• Subreddit 'r/" + subredditName + "' not found");
                MenuFormatter.printErrorMessage("• Make sure the subreddit name is correct");
            } else if (result.status == 401) {
                MenuFormatter.printErrorMessage("• Authentication failed - please log in again");
            } else if (result.status == 403) {
                MenuFormatter.printErrorMessage("• Permission denied - you can only edit subreddits you created");
            } else {
                MenuFormatter.printErrorMessage("• Error: " + result.message);
                MenuFormatter.printErrorMessage("• Status code: " + result.status);
            }
        }
    }

    private void deleteSubreddit() {
        String input = ConsoleIO.readLine("Enter the subreddit name to delete (e.g., 'technology' or 'r/technology'): ");
        if (input.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        String subredditName = input.trim();

        if (subredditName.toLowerCase().startsWith("r/")) {
            subredditName = subredditName.substring(2);
        }

        subredditName = subredditName.toLowerCase();

        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName);
        if (!subredditResult.success) {
            MenuFormatter.printErrorMessage("Error loading subreddit 'r/" + subredditName + "': " + subredditResult.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        MenuFormatter.printMenuHeader("Delete Subreddit");
        MenuFormatter.printInfoMessage("Subreddit: r/" + subreddit.name());
        MenuFormatter.printInfoMessage("Display Name: " + subreddit.displayName());
        MenuFormatter.printInfoMessage("Posts: " + subreddit.postCount());

        if (subreddit.postCount() > 0) {
            MenuFormatter.printErrorMessage("Cannot delete this subreddit because it contains " + subreddit.postCount() + " post(s).");
            MenuFormatter.printInfoMessage("You must delete all posts from this subreddit before you can delete it.");
            return;
        }

        MenuFormatter.printWarningMessage("Are you sure you want to delete this subreddit?");
        String confirm = ConsoleIO.readLine("Type 'yes' to confirm deletion: ");

        if ("yes".equalsIgnoreCase(confirm)) {
            ApiResult<String> result = subredditClient.deleteSubreddit(subredditName);
            if (result.success) {
                MenuFormatter.printSuccessMessage("Subreddit deleted successfully!");
            } else {
                MenuFormatter.printErrorMessage("Failed to delete subreddit!");

                if (result.status == 400) {
                    MenuFormatter.printErrorMessage("• Cannot delete subreddit - it may contain posts");
                    MenuFormatter.printErrorMessage("• Please delete all posts first");
                } else if (result.status == 404) {
                    MenuFormatter.printErrorMessage("• Subreddit 'r/" + subredditName + "' not found");
                } else if (result.status == 401) {
                    MenuFormatter.printErrorMessage("• Authentication failed - please log in again");
                } else if (result.status == 403) {
                    MenuFormatter.printErrorMessage("• Permission denied - you can only delete subreddits you created");
                } else if (result.status == 409) {
                    MenuFormatter.printErrorMessage("• Subreddit cannot be deleted - it may have dependencies");
                } else {
                    MenuFormatter.printErrorMessage("• Error: " + result.message);
                    MenuFormatter.printErrorMessage("• Status code: " + result.status);
                }
            }
        } else {
            MenuFormatter.printInfoMessage("Delete cancelled.");
        }
    }

    private void sortSubreddits() {
        MenuFormatter.printMenuHeader("Sort Subreddits");

        java.util.List<String> sortOptions = new java.util.ArrayList<>();
        for (SubredditSortType sortType : SubredditSortType.values()) {
            sortOptions.add(sortType.ordinal() + 1 + ". " + sortType.getDisplayName());
        }
        sortOptions.add("0. Cancel");

        MenuFormatter.printMenuOptions(sortOptions.toArray(new String[0]));

        int choice = ConsoleIO.readInt("Choose sorting option:");
        if (choice >= 1 && choice <= SubredditSortType.values().length) {
            currentSubredditSortType = SubredditSortType.values()[choice - 1];
            MenuFormatter.printInfoMessage("Sorting subreddits by " + currentSubredditSortType.getDisplayName() + "...");
            applySubredditSort();
        } else if (choice == 0) {
            MenuFormatter.printInfoMessage("Sort canceled.");
        } else {
            MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
        }
    }

    private void applySubredditSort() {
        if (allSubreddits == null) return;

        switch (currentSubredditSortType) {
            case ALPHABETIC_ASC -> allSubreddits.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
            case ALPHABETIC_DESC -> allSubreddits.sort((a, b) -> b.name().compareToIgnoreCase(a.name()));
            case POSTS_DESC -> allSubreddits.sort((a, b) -> b.postCount() - a.postCount());
            case POSTS_ASC -> allSubreddits.sort((a, b) -> a.postCount() - b.postCount());
        }

        MenuFormatter.printSuccessMessage("Subreddits sorted by " + currentSubredditSortType.getDisplayName() + ".");
        currentSubredditPage = 0; // Reset to first page after sorting
    }

    private void sortSubredditPosts() {
        MenuFormatter.printMenuHeader("Sort Posts in r/" + currentSubredditName);

        java.util.List<String> sortOptions = new java.util.ArrayList<>();
        for (PostSortType sortType : PostSortType.values()) {
            sortOptions.add(sortType.ordinal() + 1 + ". " + sortType.getDisplayName());
        }
        sortOptions.add("0. Cancel");

        MenuFormatter.printMenuOptions(sortOptions.toArray(new String[0]));

        int choice = ConsoleIO.readInt("Choose sorting option:");
        if (choice >= 1 && choice <= PostSortType.values().length) {
            currentPostSortType = PostSortType.values()[choice - 1];
            MenuFormatter.printInfoMessage("Sorting posts by " + currentPostSortType.getDisplayName() + "...");
            applyPostSort();
        } else if (choice == 0) {
            MenuFormatter.printInfoMessage("Sort canceled.");
        } else {
            MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
        }
    }

    private void applyPostSort() {
        if (allSubredditPosts == null) return;

        switch (currentPostSortType) {
            case DATE_DESC -> allSubredditPosts.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
            case DATE_ASC -> allSubredditPosts.sort((a, b) -> a.createdAt().compareTo(b.createdAt()));
            case SCORE_DESC -> allSubredditPosts.sort((a, b) -> (b.upvotes() - b.downvotes()) - (a.upvotes() - a.downvotes()));
            case COMMENTS_DESC -> allSubredditPosts.sort((a, b) -> b.commentCount() - a.commentCount());
        }

        MenuFormatter.printSuccessMessage("Posts sorted by " + currentPostSortType.getDisplayName() + ".");
        currentPostPage = 0;
    }
}
