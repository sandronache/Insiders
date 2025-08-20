package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.clients.SubredditClient;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.post.PostCreateRequestDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.TimeUtils;
import com.insiders.util.MenuFormatter;
import com.insiders.util.ContentValidator;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedMenu {
    private final PostClient postClient;
    private final SubredditClient subredditClient;
    private final SessionManager sessionManager;
    private final PostMenu postMenu;
    private final SubredditMenu subredditMenu;
    private final Map<Integer, UUID> postIdMapping = new HashMap<>();

    private static final int POSTS_PER_PAGE = 10;
    private List<PostResponseDto> allPosts = null;
    private int currentPage = 0;

    private SortType currentSortType = SortType.DATE_DESC;

    public enum SortType {
        DATE_DESC("Newest First"),
        DATE_ASC("Oldest First"),
        SCORE_DESC("Highest Score"),
        COMMENTS_DESC("Most Comments");

        private final String displayName;

        SortType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public FeedMenu(PostClient postClient, SubredditClient subredditClient, SessionManager sessionManager) {
        this.postClient = postClient;
        this.subredditClient = subredditClient;
        this.sessionManager = sessionManager;
        this.postMenu = new PostMenu(postClient, sessionManager);
        this.subredditMenu = new SubredditMenu(postClient, subredditClient, sessionManager);
    }

    public void showMenu() {
        MenuFormatter.printWelcomeHeader(sessionManager.username());
        viewAllPosts();

        while (true) {
            displayPostsAndNavigate();

            MenuFormatter.printMenuHeader("Feed Menu");

            // Build menu options with sequential numbering
            java.util.List<String> menuOptions = new java.util.ArrayList<>();
            menuOptions.add("1. Refresh Posts");
            menuOptions.add("2. Create New Post");
            menuOptions.add("3. Enter Post");
            menuOptions.add("4. Subreddit Actions");
            menuOptions.add("5. Sort Posts");

            int nextOptionNumber = 6;
            int previousPageOption = -1;
            int nextPageOption = -1;

            if (currentPage > 0) {
                previousPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Previous Page");
                nextOptionNumber++;
            }
            if (allPosts != null && (currentPage + 1) * POSTS_PER_PAGE < allPosts.size()) {
                nextPageOption = nextOptionNumber;
                menuOptions.add(nextOptionNumber + ". Next Page");
                nextOptionNumber++;
            }

            menuOptions.add("0. Back to Main Menu");

            MenuFormatter.printMenuOptions(menuOptions.toArray(new String[0]));

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> {
                    MenuFormatter.printInfoMessage("Refreshing posts...");
                    viewAllPosts();
                }
                case 2 -> createPost();
                case 3 -> enterPostId();
                case 4 -> subredditActions();
                case 5 -> sortPosts();
                default -> {
                    if (choice == previousPageOption && currentPage > 0) {
                        currentPage--;
                        MenuFormatter.printInfoMessage("Going to previous page...");
                    } else if (choice == nextPageOption && allPosts != null && (currentPage + 1) * POSTS_PER_PAGE < allPosts.size()) {
                        currentPage++;
                        MenuFormatter.printInfoMessage("Going to next page...");
                    } else if (choice == 0) {
                        return;
                    } else {
                        MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
                    }
                }
            }
        }
    }

    private void viewAllPosts() {
        ApiResult<List<PostResponseDto>> result = postClient.getAllPosts();
        if (result.success) {
            allPosts = result.data;
            currentPage = 0;
            applySort();
        } else {
            MenuFormatter.printErrorMessage("Error loading posts: " + result.message);
        }
    }

    private void enterPostId() {
        String input = ConsoleIO.readLine("Enter post ID: ");
        try {
            int simpleId = Integer.parseInt(input);

            UUID actualPostId = postIdMapping.get(simpleId);
            if (actualPostId != null) {
                postMenu.showPostManagementMenu(actualPostId);
                MenuFormatter.printInfoMessage("Returning to feed...");
                viewAllPosts();
            } else {
                MenuFormatter.printErrorMessage("Invalid post ID! Please choose a number from the list above.");
            }
        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the post list.");
        }
    }

    private void displayPostsAndNavigate() {
        if (allPosts == null || allPosts.isEmpty()) {
            MenuFormatter.printInfoMessage("No posts found.");
            return;
        }

        postIdMapping.clear();
        MenuFormatter.printPostHeader();

        MenuFormatter.printInfoMessage("Currently sorted by: " + currentSortType.getDisplayName());

        int start = currentPage * POSTS_PER_PAGE;
        int end = Math.min(start + POSTS_PER_PAGE, allPosts.size());

        for (int i = start; i < end; i++) {
            PostResponseDto post = allPosts.get(i);
            int simpleId = (i - start) + 1; // Use relative position on current page
            postIdMapping.put(simpleId, post.id());

            boolean isOwnPost = post.author().equals(sessionManager.username());
            int score = post.upvotes() - post.downvotes();
            String timeAgo = TimeUtils.getRelativeTime(post.createdAt().toString());

            MenuFormatter.printPostCard(
                    simpleId,
                    post.title(),
                    post.content(),
                    post.author(),
                    isOwnPost,
                    post.subreddit(),
                    score,
                    post.commentCount(),
                    timeAgo,
                    post.imageUrl()
            );
        }

        int totalPages = (int) Math.ceil((double) allPosts.size() / POSTS_PER_PAGE);
        MenuFormatter.printInfoMessage(String.format("Showing page %d of %d (%d total posts)",
                currentPage + 1, totalPages, allPosts.size()));

        MenuFormatter.printInfoMessage("Currently sorted by: " + currentSortType.getDisplayName());
    }

    private void createPost() {
        MenuFormatter.printCreatePostHeader();

        // Title validation (required)
        String title;
        while (true) {
            title = ConsoleIO.readLine("Enter post title: ");
            if (ContentValidator.isValidTitle(title)) {
                title = title.trim();
                break;
            } else {
                MenuFormatter.printErrorMessage(ContentValidator.getTitleErrorMessage(title));
                MenuFormatter.printInfoMessage("Title requirements: 3-300 characters, no inappropriate content.");
            }
        }

        // Content validation (optional, but must be valid if provided)
        String content = null;
        String contentInput = ConsoleIO.readLine("Enter post content (optional, press Enter to skip): ");
        if (!contentInput.trim().isEmpty()) {
            while (true) {
                if (ContentValidator.isValidContent(contentInput)) {
                    content = contentInput.trim();
                    break;
                } else {
                    MenuFormatter.printErrorMessage(ContentValidator.getContentErrorMessage(contentInput));
                    MenuFormatter.printInfoMessage("Content requirements: maximum 10000 characters, no inappropriate content.");
                    contentInput = ConsoleIO.readLine("Enter valid content (or press Enter to skip): ");
                    if (contentInput.trim().isEmpty()) {
                        content = null;
                        break;
                    }
                }
            }
        }

        String subreddit;
        while (true) {
            subreddit = ConsoleIO.readLine("Enter subreddit name: ");

            // First check format validation
            if (!ContentValidator.isValidSubredditName(subreddit)) {
                MenuFormatter.printErrorMessage(ContentValidator.getSubredditNameErrorMessage(subreddit));
                MenuFormatter.printInfoMessage("Subreddit requirements: 3-50 characters, alphanumeric and underscore only.");
                continue;
            }

            subreddit = subreddit.toLowerCase().trim();

            // Then check if subreddit exists
            MenuFormatter.printInfoMessage("Checking if subreddit 'r/" + subreddit + "' exists...");
            ApiResult<com.insiders.dto.subreddit.SubredditResponseDto> subredditCheck = subredditClient.getSubredditByName(subreddit);

            if (subredditCheck.success) {
                MenuFormatter.printInfoMessage("✓ Subreddit 'r/" + subreddit + "' found!");
                break;
            } else {
                if (subredditCheck.status == 404) {
                    MenuFormatter.printErrorMessage("✗ Subreddit 'r/" + subreddit + "' does not exist!");
                    MenuFormatter.printInfoMessage("Please create the subreddit first or choose an existing one.");
                    MenuFormatter.printInfoMessage("Returning to feed menu...");
                    return;
                } else {
                    MenuFormatter.printErrorMessage("Error checking subreddit: " + subredditCheck.message);
                    MenuFormatter.printInfoMessage("Please try again or contact support if the problem persists.");
                }
            }
        }

        String author = sessionManager.username();

        PostCreateRequestDto createRequest = new PostCreateRequestDto(title, content, author, subreddit);

        MenuFormatter.printInfoMessage("Creating post...");
        ApiResult<PostResponseDto> result = postClient.createPost(createRequest);

        if (result.success) {
            PostResponseDto createdPost = result.data;
            MenuFormatter.printSuccessMessage("Post created successfully!");
            MenuFormatter.printInfoMessage("Post ID: " + createdPost.id() + "\nTitle: " + createdPost.title());
            postMenu.showPostManagementMenu(createdPost.id());
            MenuFormatter.printInfoMessage("Returning to feed...");
            viewAllPosts();
        } else {
            MenuFormatter.printErrorMessage("Error creating post: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void subredditActions() {
        subredditMenu.showSubredditActions();
    }

    private void sortPosts() {
        MenuFormatter.printMenuHeader("Sort Posts");

        java.util.List<String> sortOptions = new java.util.ArrayList<>();
        for (SortType sortType : SortType.values()) {
            sortOptions.add(sortType.ordinal() + 1 + ". " + sortType.getDisplayName());
        }
        sortOptions.add("0. Cancel");

        MenuFormatter.printMenuOptions(sortOptions.toArray(new String[0]));

        int choice = ConsoleIO.readInt("Choose sorting option:");
        if (choice >= 1 && choice <= SortType.values().length) {
            currentSortType = SortType.values()[choice - 1];
            MenuFormatter.printInfoMessage("Sorting posts by " + currentSortType.getDisplayName() + "...");
            applySort();
        } else if (choice == 0) {
            MenuFormatter.printInfoMessage("Sort canceled.");
        } else {
            MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
        }
    }

    private void applySort() {
        if (allPosts == null) return;

        switch (currentSortType) {
            case DATE_DESC -> allPosts.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
            case DATE_ASC -> allPosts.sort((a, b) -> a.createdAt().compareTo(b.createdAt()));
            case SCORE_DESC -> allPosts.sort((a, b) -> (b.upvotes() - b.downvotes()) - (a.upvotes() - a.downvotes()));
            case COMMENTS_DESC -> allPosts.sort((a, b) -> b.commentCount() - a.commentCount());
        }

        MenuFormatter.printInfoMessage("Posts sorted by " + currentSortType.getDisplayName() + ".");
        currentPage = 0;
    }

    private boolean createSubredditPrompt(String subredditName) {
        MenuFormatter.printMenuHeader("Quick Subreddit Creation");
        MenuFormatter.printInfoMessage("Creating subreddit: r/" + subredditName);

        try {
            String displayName;
            while (true) {
                displayName = ConsoleIO.readLine("Enter display name for r/" + subredditName + ": ");
                if (ContentValidator.isValidTitle(displayName)) {
                    displayName = displayName.trim();
                    break;
                } else {
                    MenuFormatter.printErrorMessage(ContentValidator.getTitleErrorMessage(displayName));
                    MenuFormatter.printInfoMessage("Display name requirements: 3-300 characters, no inappropriate content.");
                }
            }

            String description;
            while (true) {
                description = ConsoleIO.readLine("Enter description for r/" + subredditName + ": ");
                if (ContentValidator.isValidContent(description)) {
                    description = description.trim();
                    break;
                } else {
                    MenuFormatter.printErrorMessage(ContentValidator.getContentErrorMessage(description));
                    MenuFormatter.printInfoMessage("Description requirements: maximum 10000 characters, no inappropriate content.");
                }
            }

            com.insiders.dto.subreddit.SubredditCreateRequestDto createRequest =
                new com.insiders.dto.subreddit.SubredditCreateRequestDto(subredditName, displayName, description, null);

            MenuFormatter.printInfoMessage("Creating subreddit r/" + subredditName + "...");
            ApiResult<com.insiders.dto.subreddit.SubredditResponseDto> result = subredditClient.createSubreddit(createRequest);

            if (result.success) {
                MenuFormatter.printSuccessMessage("Subreddit r/" + subredditName + " created successfully!");
                return true;
            } else {
                MenuFormatter.printErrorMessage("Failed to create subreddit: " + result.message);
                if (result.status == 409) {
                    MenuFormatter.printInfoMessage("The subreddit name is already taken.");
                } else if (result.status == 401) {
                    MenuFormatter.printInfoMessage("Authentication failed. Please log in again.");
                }
                return false;
            }
        } catch (Exception e) {
            MenuFormatter.printErrorMessage("Error creating subreddit: " + e.getMessage());
            return false;
        }
    }

    private void showAvailableSubreddits() {
        MenuFormatter.printMenuHeader("Available Subreddits");
        MenuFormatter.printInfoMessage("Loading available subreddits...");

        ApiResult<List<com.insiders.dto.subreddit.SubredditResponseDto>> result = subredditClient.getAllSubreddits();

        if (result.success && result.data != null) {
            if (result.data.isEmpty()) {
                MenuFormatter.printInfoMessage("No subreddits exist yet. You could be the first to create one!");
            } else {
                MenuFormatter.printInfoMessage("Available subreddits (" + result.data.size() + " total):");

                // Show first 10 subreddits for quick reference
                int count = Math.min(10, result.data.size());
                for (int i = 0; i < count; i++) {
                    com.insiders.dto.subreddit.SubredditResponseDto subreddit = result.data.get(i);
                    MenuFormatter.printInfoMessage("• r/" + subreddit.name() + " - " + subreddit.displayName());
                }

                if (result.data.size() > 10) {
                    MenuFormatter.printInfoMessage("... and " + (result.data.size() - 10) + " more.");
                    MenuFormatter.printInfoMessage("Use 'Subreddit Actions' → 'All Subreddits' to see the complete list.");
                }
            }
        } else {
            MenuFormatter.printErrorMessage("Could not load subreddits: " + (result.message != null ? result.message : "Unknown error"));
        }

        MenuFormatter.printInfoMessage("Press Enter to continue...");
        ConsoleIO.readLine("");
    }
}
