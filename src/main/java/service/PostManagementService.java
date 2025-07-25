package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Post;
import main.java.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Service responsible for post management operations
 */

@Service
public class PostManagementService {
    private final PostRepository postRepository;
    private final ContentService contentService;
    private final DatabaseMappingService mappingService;
    private final CommentService commentService;

    @Autowired
    public PostManagementService(PostRepository postRepository, ContentService contentService, DatabaseMappingService mappingService, CommentService commentService) {
        this.postRepository = postRepository;
        this.contentService = contentService;
        this.mappingService = mappingService;
        this.commentService = commentService;
    }

    public TreeMap<UUID, Post> loadPostsFromDatabase() {
        TreeMap<UUID, Post> posts = new TreeMap<>();

        try {
            // Use the new JPA method to get all posts ordered by creation date
            List<Post> allPosts = postRepository.findAllByOrderByCreatedAtDesc();

            for (Post post : allPosts) {
                posts.put(post.getId(), post);

                // Store mapping using the post's UUID (optional, if needed)
                mappingService.storePostMapping(post.getId(), post.getId());

                // Load comments for this post
                commentService.loadCommentsForPost(post, post.getId());

                // Load votes for this post
                contentService.loadVotesForPost(post, post.getId());

                // Load votes for all comments in this post
                post.getComments().forEach((commentId, comment) ->
                        commentService.loadVotesForComment(comment)
                );
            }

            LoggerFacade.info("Loaded " + allPosts.size() + " posts from database");
        } catch (Exception e) {
            LoggerFacade.warning("Could not load posts from database: " + e.getMessage());
            LoggerFacade.info("Starting with empty posts list");
        }

        return posts;
    }

    public void addPost(AppData appData, String content) {
        String username = appData.getLoggedUser().getUsername();

        // Check for duplicates
        try {
            if (postExistsInDatabase(content, username)) {
                LoggerFacade.warning("Post with same content already exists, skipping save");
                return;
            }
        } catch (Exception e) {
            LoggerFacade.warning("Could not check for duplicate posts: " + e.getMessage());
        }

        // Create post using the legacy method (with default title and subreddit)
        Post post = contentService.createPost(content, username);

        // Add to in-memory data
        appData.getLoadedPosts().put(post.getId(), post);

        // Save to database immediately
        try {
            postRepository.save(post);
            LoggerFacade.info("Post saved to database immediately: " + post.getId());
        } catch (Exception e) {
            LoggerFacade.warning("Could not save post to database immediately: " + e.getMessage());
        }

        LoggerFacade.info("New post created by user: " + username);
    }

    // New method for creating posts with all fields (for API compatibility)
    public void addPost(AppData appData, String title, String content, String username, String subreddit) {
        // Check for duplicates
        try {
            if (postExistsInDatabase(content, username)) {
                LoggerFacade.warning("Post with same content already exists, skipping save");
                return;
            }
        } catch (Exception e) {
            LoggerFacade.warning("Could not check for duplicate posts: " + e.getMessage());
        }

        // Create post using the new method with all fields
        Post post = contentService.createPost(title, content, username, subreddit);

        // Add to in-memory data
        appData.getLoadedPosts().put(post.getId(), post);

        // Save to database immediately
        try {
            postRepository.save(post);
            LoggerFacade.info("Post saved to database immediately: " + post.getId());
        } catch (Exception e) {
            LoggerFacade.warning("Could not save post to database immediately: " + e.getMessage());
        }

        LoggerFacade.info("New post created by user: " + username + " in subreddit: " + subreddit);
    }

    public void deletePost(AppData appData, UUID idx) {
        Post post = appData.getLoadedPosts().get(idx);
        String currentUser = appData.getLoggedUser().getUsername();

        if (post == null) {
            LoggerFacade.warning("No post at index " + idx);
            return;
        }

        if (!post.getUsername().equals(currentUser)) {
            LoggerFacade.warning("Unauthorized deletion attempt by " + currentUser);
            return;
        }

        appData.getLoadedPosts().remove(idx);
        LoggerFacade.info("Post " + idx + " deleted by owner " + currentUser);
    }

    private boolean postExistsInDatabase(String content, String username) {
        try {
            List<Post> existingPosts = postRepository.findByUsernameOrderByCreatedAtDesc(username);
            return existingPosts.stream()
                    .anyMatch(post -> post.getContent().equals(content));
        } catch (Exception e) {
            LoggerFacade.warning("Error checking for existing posts: " + e.getMessage());
            return false;
        }
    }
}
