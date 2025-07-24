package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Post;
import main.java.model.User;
import main.java.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import main.java.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Service
public class AppDataService {
    private static AppDataService instance;
    private final PostManagementService postService;
    private final ContentService contentService;
    private final AppData appData;
    private final UserRepository userRepository;

    @Autowired
    private AppDataService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.postService = PostManagementService.getInstance();
        this.contentService = ContentService.getInstance();
        this.appData = createAppData();
    }

    public AppData getAppData() { return this.appData; }

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
        HashMap<String, User> registeredUsers = loadUsersFromDatabase();

        return new AppData(loadedPosts, idNextPost, registeredUsers);
    }

    private HashMap<String, User> loadUsersFromDatabase() {
        HashMap<String, User> users = new HashMap<>();

        try {
            List<User> userList = userRepository.findAll();

            for (User user : userList) {
                users.put(user.getUsername(), user);
            }

            LoggerFacade.info("Loaded " + userList.size() + " users from database");
        } catch (Exception e) {
            LoggerFacade.warning("Could not load users from database: " + e.getMessage());
            LoggerFacade.info("Starting with empty user list");
        }

        return users;
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
