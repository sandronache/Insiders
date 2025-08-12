package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.dto.post.PostResponseDto;
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
        System.out.println("\n=== Latest Posts ===");
        viewAllPosts();

        while (true) {
            System.out.println("\n--- Feed Menu ---");
            System.out.println("1. Refresh Posts");
            System.out.println("2. View Posts by Subreddit");
            System.out.println("3. Enter Post");
            System.out.println("0. Back");

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> viewAllPosts();
                case 2 -> viewPostsBySubreddit();
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

            System.out.println("ID: " + simpleId + " (UUID: " + post.id() + ")");
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

    private void viewPostsBySubreddit() {
        String subreddit = ConsoleIO.readLine("Enter subreddit name: ");
        ApiResult<List<PostResponseDto>> result = postClient.getPostsBySubreddit(subreddit);
        if (result.success) {
            displayPostsAndNavigate(result.data);
        } else {
            System.out.println("Error loading posts: " + result.message);
        }
    }
}
