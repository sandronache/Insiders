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
            MenuFormatter.printMenuHeader("Feed Menu");
            MenuFormatter.printMenuOptions(
                "1. Refresh Posts",
                "2. Create New Post",
                "3. Enter Post",
                "4. Subreddit Actions",
                "0. Back to Main Menu"
            );

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> {
                    MenuFormatter.printInfoMessage("Refreshing posts...");
                    viewAllPosts();
                }
                case 2 -> createPost();
                case 3 -> enterPostId();
                case 4 -> subredditActions();
                case 0 -> {
                    return;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
            }
        }
    }

    private void viewAllPosts() {
        ApiResult<List<PostResponseDto>> result = postClient.getAllPosts();
        if (result.success) {
            displayPostsAndNavigate(result.data);
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

    private void displayPostsAndNavigate(List<PostResponseDto> posts) {
        if (posts.isEmpty()) {
            MenuFormatter.printInfoMessage("No posts found.");
            return;
        }

        postIdMapping.clear();
        MenuFormatter.printPostHeader();

        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto post = posts.get(i);
            int simpleId = i + 1;
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
}
