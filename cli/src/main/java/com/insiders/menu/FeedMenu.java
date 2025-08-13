package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.post.PostCreateRequestDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedMenu {
    private final PostClient postClient;
    private final SessionManager sessionManager;
    private final PostMenu postMenu;
    private Map<Integer, UUID> postIdMapping = new HashMap<>();

    public FeedMenu(PostClient postClient, SessionManager sessionManager) {
        this.postClient = postClient;
        this.sessionManager = sessionManager;
        this.postMenu = new PostMenu(postClient, sessionManager);
    }

    public void showMenu() {
        System.out.println("\n=== Welcome " + sessionManager.username() + "! ===");
        System.out.println("=== Latest Posts ===");
        viewAllPosts();

        while (true) {
            System.out.println("\n--- Feed Menu ---");
            System.out.println("1. Refresh Posts");
            System.out.println("2. Create New Post");
            System.out.println("3. Enter Post");
            System.out.println("0. Back");

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> viewAllPosts();
                case 2 -> createPost();
                case 3 -> enterPostId();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again!");
            }
        }
    }

    private void viewAllPosts() {
        ApiResult<List<PostResponseDto>> result = postClient.getAllPosts();
        if (result.success) {
            displayPostsAndNavigate(result.data);
        } else {
            System.out.println("Error loading posts: " + result.message);
        }
    }

    private void enterPostId() {
        String input = ConsoleIO.readLine("Enter post ID: ");
        try {
            int simpleId = Integer.parseInt(input);

            UUID actualPostId = postIdMapping.get(simpleId);
            if (actualPostId != null) {
                postMenu.showPostManagementMenu(actualPostId);
            } else {
                System.out.println("Invalid post ID! Please choose a number from the list above.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the post list.");
        }
    }

    private void displayPostsAndNavigate(List<PostResponseDto> posts) {
        if (posts.isEmpty()) {
            System.out.println("No posts found.");
            return;
        }

        postIdMapping.clear();

        System.out.println("\n--- Posts Feed ---");
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto post = posts.get(i);
            int simpleId = i + 1;

            postIdMapping.put(simpleId, post.id());

            System.out.println("ID: " + simpleId);
            System.out.println("Title: " + post.title());
            System.out.println("Author: " + post.author());

            if (post.author().equals(sessionManager.username())) {
                System.out.println("📝 [YOUR POST]");
            }

            System.out.println("Subreddit: " + post.subreddit());
            System.out.println("Upvotes: " + post.upvotes() + " | Downvotes: " + post.downvotes());
            System.out.println("---");
        }
    }


    private void createPost() {
        System.out.println("\n=== Create New Post ===");

        String title = ConsoleIO.readLine("Enter post title: ");
        if (title.trim().isEmpty()) {
            System.out.println("Title cannot be empty!");
            return;
        }

        String content = ConsoleIO.readLine("Enter post content: ");
        String subreddit = ConsoleIO.readLine("Enter subreddit: ");
        if (subreddit.trim().isEmpty()) {
            System.out.println("Subreddit cannot be empty!");
            return;
        }

        String author = sessionManager.username();

        PostCreateRequestDto createRequest = new PostCreateRequestDto(title, content, author, subreddit);

        System.out.println("Creating post...");
        ApiResult<PostResponseDto> result = postClient.createPost(createRequest);

        if (result.success) {
            PostResponseDto createdPost = result.data;
            System.out.println("Post created successfully!");
            System.out.println("Post ID: " + createdPost.id());
            System.out.println("Title: " + createdPost.title());

            String viewPost = ConsoleIO.readLine("Do you want to view/manage the created post? (y/n): ");
            if ("y".equalsIgnoreCase(viewPost.trim())) {
                postMenu.showPostManagementMenu(createdPost.id());
            }

            System.out.println("\n=== Updated Feed ===");
            viewAllPosts();
        } else {
            System.out.println("Error creating post: " + result.message + " (Status: " + result.status + ")");
        }
    }
}
