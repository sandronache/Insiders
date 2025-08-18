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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedMenu {
    private final PostClient postClient;
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
                post.author(),
                isOwnPost,
                post.subreddit(),
                score,
                post.commentCount(),
                timeAgo
            );
        }

        int totalPages = (int) Math.ceil((double) allPosts.size() / POSTS_PER_PAGE);
        MenuFormatter.printInfoMessage(String.format("Showing page %d of %d (%d total posts)",
            currentPage + 1, totalPages, allPosts.size()));

        MenuFormatter.printInfoMessage("Currently sorted by: " + currentSortType.getDisplayName());
    }

    private void createPost() {
        MenuFormatter.printCreatePostHeader();

        String title = ConsoleIO.readLine("Enter post title: ");
        if (title.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Title cannot be empty!");
            return;
        }

        String content = ConsoleIO.readLine("Enter post content: ");
        String subreddit = ConsoleIO.readLine("Enter subreddit: ");
        if (subreddit.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit cannot be empty!");
            return;
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
}
