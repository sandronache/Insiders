package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Post;
import main.java.model.User;
import main.java.util.Helper;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Main application data service - orchestrates other specialized services
 */
public class AppDataService {
    private static AppDataService instance;
    private final UserManagementService userService;
    private final PostManagementService postService;
    private final ContentService contentService;

    private AppDataService() {
        this.userService = UserManagementService.getInstance();
        this.postService = PostManagementService.getInstance();
        this.contentService = ContentService.getInstance();
    }

    public static AppDataService getInstance() {
        if (instance == null) {
            instance = new AppDataService();
        }
        return instance;
    }

    public AppData createAppData() {
        LoggerFacade.info("Creating new application data");

        // Load posts from database (comments are loaded automatically)
        TreeMap<Integer, Post> loadedPosts = postService.loadPostsFromDatabase();

        // Get next post ID
        int idNextPost = 0;
        if (!loadedPosts.isEmpty()) {
            idNextPost = loadedPosts.lastKey() + 1;
        }

        // Load users from database
        HashMap<String, User> registeredUsers = userService.loadUsersFromDatabase();

        return new AppData(loadedPosts, idNextPost, registeredUsers);
    }

    public void writeAppData(AppData appData) {
        LoggerFacade.info("Saving application data to database");
        userService.saveUsersToDatabase(appData);
        LoggerFacade.info("Posts and comments are already saved to database when created");
    }

    // User management delegation
    public boolean register(AppData appData, String username, String email, String password) {
        return userService.register(appData, username, email, password);
    }

    public boolean login(AppData appData, String username, String password) {
        return userService.login(appData, username, password);
    }

    public void logout(AppData appData) {
        userService.logout(appData);
    }

    public void deleteUser(AppData appData) {
        userService.deleteUser(appData);
    }

    // Post management delegation
    public void addPost(AppData appData, String content) {
        postService.addPost(appData, content);
    }

    public void deletePost(AppData appData, int idx) {
        postService.deletePost(appData, idx);
    }

    // Comment management using ContentService
    public void addComment(AppData appData, int postId, String content) {
        Post post = appData.getLoadedPosts().get(postId);
        if (post != null) {
            String username = appData.getLoggedUser().getUsername();
            contentService.addComment(post, content, username);
        } else {
            LoggerFacade.warning("Cannot add comment to non-existent post: " + postId);
        }
    }

    public void addReply(AppData appData, int postId, int commentId, String content) {
        Post post = appData.getLoadedPosts().get(postId);
        if (post != null) {
            String username = appData.getLoggedUser().getUsername();

            // For direct replies to comments, use ContentService with database persistence
            if (Helper.extractRemainingLevels(commentId + "").isEmpty()) {
                // This is a direct reply to a comment - implement database saving logic here
                LoggerFacade.info("Direct reply to comment - would need database integration");
            }

            // For now, use existing ContentService logic for nested replies
            String commentIdStr = commentId + "";
            contentService.addReply(post, commentIdStr, content, username);
        } else {
            LoggerFacade.warning("Cannot add reply to non-existent post: " + postId);
        }
    }

    // Rendering function
    public String renderFeed(AppData appData) {
        LoggerFacade.debug("Rendering feed with " + appData.getLoadedPosts().size() + " posts");

        StringBuilder feed = new StringBuilder();

        appData.getLoadedPosts().forEach((id, post) ->
                feed.append(contentService.renderFeedPost(post, id.toString()))
                        .append("\n"));

        return feed.toString();
    }
}
