package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Post;
import main.java.repository.PostRepository;

import java.util.List;
import java.util.TreeMap;

/**
 * Service responsible for post management operations
 */
public class PostManagementService {
    private static PostManagementService instance;
    private final PostRepository postRepository;
    private final ContentService contentService;
    private final DatabaseMappingService mappingService;

    private PostManagementService() {
        this.postRepository = new PostRepository();
        this.contentService = ContentService.getInstance();
        this.mappingService = DatabaseMappingService.getInstance();
    }

    public static PostManagementService getInstance() {
        if (instance == null) {
            instance = new PostManagementService();
        }
        return instance;
    }

    public TreeMap<Integer, Post> loadPostsFromDatabase() {
        TreeMap<Integer, Post> posts = new TreeMap<>();

        try {
            List<PostRepository.PostWithId> postsWithIds = postRepository.findAllOrderedByDateWithIds();

            int index = 0;
            for (PostRepository.PostWithId postWithId : postsWithIds) {
                Post post = postWithId.getPost();
                Integer databaseId = postWithId.getDatabaseId();

                posts.put(index, post);
                mappingService.storePostMapping(index, databaseId);

                // Load comments for this post
                CommentService commentService = CommentService.getInstance();
                commentService.loadCommentsForPost(post, databaseId);

                index++;
            }

            LoggerFacade.info("Loaded " + postsWithIds.size() + " posts from database");
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

        Post post = contentService.createPost(content, username);
        Integer id = appData.getIdNextPost();
        appData.setIdNextPost(id + 1);

        // Add to in-memory data
        appData.getLoadedPosts().put(id, post);

        // Save to database immediately
        try {
            postRepository.save(post);
            LoggerFacade.info("Post saved to database immediately: " + id);
        } catch (Exception e) {
            LoggerFacade.warning("Could not save post to database immediately: " + e.getMessage());
        }

        LoggerFacade.info("New post created by user: " + username);
    }

    public void deletePost(AppData appData, int idx) {
        if (appData.getLoadedPosts().containsKey(idx)) {
            LoggerFacade.info("Post with index " + idx + " deleted");
            appData.getLoadedPosts().remove(idx);
        } else {
            LoggerFacade.warning("Attempt to delete non-existent post at index: " + idx);
        }
    }

    private boolean postExistsInDatabase(String content, String username) {
        try {
            List<Post> existingPosts = postRepository.findByUsername(username);
            return existingPosts.stream()
                    .anyMatch(post -> post.getContent().equals(content));
        } catch (Exception e) {
            LoggerFacade.warning("Error checking for existing posts: " + e.getMessage());
            return false;
        }
    }
}
