package main.java.controller;

import main.java.logger.LoggerFacade;
import main.java.model.Post;
import main.java.service.AppDataService;
import main.java.service.PostManagementService;
import main.java.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostManagementService postManagementService;
    private final AppDataService appDataService;
    private final PostRepository postRepository;

    @Autowired
    public PostController(PostManagementService postManagementService, AppDataService appDataService, PostRepository postRepository) {
        this.postManagementService = postManagementService;
        this.appDataService = appDataService;
        this.postRepository = postRepository;
        LoggerFacade.info("PostController initialized with endpoints:");
        LoggerFacade.info("- GET /api/posts");
        LoggerFacade.info("- POST /api/posts");
        LoggerFacade.info("- POST /api/posts/test");
        LoggerFacade.info("- POST /api/posts/test-raw");
        LoggerFacade.info("- POST /api/posts/test-simple");
    }

    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        LoggerFacade.info("GET /api/posts called");
        return postRepository.findAllByOrderByCreatedAtDesc();
    }// Alternative mapping with trailing slash for GET
    @GetMapping("/posts/")
    public List<Post> getAllPostsWithSlash() {
        LoggerFacade.info("GET /api/posts/ called (with trailing slash)");
        return getAllPosts();
    }

    // New endpoint matching frontend API specification - simplified version
    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, String> requestMap) {
        LoggerFacade.info("=== POST /api/posts endpoint HIT ===");
        LoggerFacade.info("PostController.createPost method called");

        try {
            LoggerFacade.info("=== POST /api/posts - Creating new post ===");
            LoggerFacade.info("Raw request body: " + requestMap);
            LoggerFacade.info("Request received - Title: " + requestMap.get("title"));
            LoggerFacade.info("Request received - Content: " + requestMap.get("content"));
            LoggerFacade.info("Request received - Subreddit: " + requestMap.get("subreddit"));

            if (requestMap.isEmpty()) {
                LoggerFacade.error("POST /api/posts failed - Request body is empty");
                return createErrorResponse("Request body is required", HttpStatus.BAD_REQUEST);
            }

            // Check if user is logged in
            if (appDataService.getAppData().getLoggedUser() == null) {
                LoggerFacade.error("POST /api/posts failed - No user logged in");
                return createErrorResponse("User must be logged in to create posts", HttpStatus.UNAUTHORIZED);
            }

            LoggerFacade.info("User is logged in: " + appDataService.getAppData().getLoggedUser().getUsername());

            // Get values from map
            String title = requestMap.get("title");
            String content = requestMap.get("content");
            String subreddit = requestMap.get("subreddit");

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                LoggerFacade.error("POST /api/posts failed - Title validation failed (null or empty)");
                return createErrorResponse("Title is required", HttpStatus.BAD_REQUEST);
            }

            // Set default subreddit if not provided
            if (subreddit == null || subreddit.trim().isEmpty()) {
                subreddit = "general";
                LoggerFacade.info("Set default subreddit to: general");
            }

            // Validate field lengths
            if (title.length() < 3 || title.length() > 300) {
                LoggerFacade.error("POST /api/posts failed - Title length validation failed: " + title.length() + " characters");
                return createErrorResponse("Title must be between 3 and 300 characters", HttpStatus.BAD_REQUEST);
            }
            if (content != null && content.length() > 10000) {
                LoggerFacade.error("POST /api/posts failed - Content length validation failed: " + content.length() + " characters");
                return createErrorResponse("Content must not exceed 10000 characters", HttpStatus.BAD_REQUEST);
            }

            // Get author from logged user
            String author = appDataService.getAppData().getLoggedUser().getUsername();
            LoggerFacade.info("Creating post with author: " + author + ", title: " + title + ", subreddit: " + subreddit);

            Post newPost = new Post();
            newPost.setTitle(title);
            newPost.setContent(content);
            newPost.setSubreddit(subreddit);
            newPost.setUsername(author);
            Post savedPost = postRepository.save(newPost);
            LoggerFacade.info("Post salvat în baza de date cu ID: " + savedPost.getId());

            // Return success response matching frontend API specification
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedPost);
            LoggerFacade.info("=== POST /api/posts completed successfully ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LoggerFacade.fatal("POST /api/posts failed with exception: " + e.getMessage());
            LoggerFacade.fatal("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return createErrorResponse("Failed to create post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Alternative mapping with trailing slash for POST
    @PostMapping("/posts/")
    public ResponseEntity<Map<String, Object>> createPostWithSlash(@RequestBody Map<String, String> requestMap) {
        LoggerFacade.info("=== POST /api/posts/ endpoint HIT (with trailing slash) ===");
        return createPost(requestMap);
    }

    // Test endpoint to verify logging works
    @PostMapping("/posts/test")
    public ResponseEntity<String> testPost() {
        LoggerFacade.info("=== POST /api/posts/test endpoint HIT ===");
        LoggerFacade.info("Test endpoint called successfully");
        return ResponseEntity.ok("Test POST endpoint works!");
    }

    // Test endpoint with JSON body
    @PostMapping("/posts/test-json")
    public ResponseEntity<String> testPostJson(@RequestBody PostCreateRequest request) {
        LoggerFacade.info("=== POST /api/posts/test-json endpoint HIT ===");
        LoggerFacade.info("Received title: " + (request.getTitle() != null ? request.getTitle() : "null"));
        LoggerFacade.info("Received content: " + (request.getContent() != null ? request.getContent() : "null"));
        return ResponseEntity.ok("JSON test works! Title: " + request.getTitle());
    }

    // Test endpoint with JSON body - simplified
    @PostMapping("/posts/test-simple")
    public ResponseEntity<String> testPostSimple(@RequestBody Map<String, String> request) {
        LoggerFacade.info("=== POST /api/posts/test-simple endpoint HIT ===");
        LoggerFacade.info("Received request map: " + request);
        LoggerFacade.info("Title from map: " + request.get("title"));
        LoggerFacade.info("Content from map: " + request.get("content"));
        return ResponseEntity.ok("Simple JSON test works! Title: " + request.get("title"));
    }

    // Test endpoint with raw string body
    @PostMapping("/posts/test-raw")
    public ResponseEntity<String> testPostRaw(@RequestBody String request) {
        LoggerFacade.info("=== POST /api/posts/test-raw endpoint HIT ===");
        LoggerFacade.info("Received raw request: " + request);
        return ResponseEntity.ok("Raw test works! Body: " + request);
    }

    // Legacy endpoint for backwards compatibility
    @PostMapping("/posts/legacy")
    public String createLegacyPost(@RequestParam String content) {
        if (appDataService.getAppData().getLoggedUser() == null) {
            return "No user logged in";
        }
        postManagementService.addPost(appDataService.getAppData(), content);
        return "Post created successfully";
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable UUID postId) {
        if (appDataService.getAppData().getLoggedUser() == null) {
            return createErrorResponse("No user logged in", HttpStatus.UNAUTHORIZED);
        }
        if (!postRepository.existsById(postId)) {
            return createErrorResponse("Post not found", HttpStatus.NOT_FOUND);
        }
        postRepository.deleteById(postId);
        LoggerFacade.info("Post deleted from database: " + postId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Post deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}")
    public Post getPostById(@PathVariable UUID postId) {
        return appDataService.getAppData().getLoadedPosts().get(postId);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable UUID id,
            @RequestBody Map<String, String> requestMap) {
        LoggerFacade.info("=== PUT /api/posts/" + id + " endpoint HIT ===");
        try {
            if (appDataService.getAppData().getLoggedUser() == null) {
                LoggerFacade.error("PUT /api/posts failed - No user logged in");
                return createErrorResponse("User must be logged in to edit posts", HttpStatus.UNAUTHORIZED);
            }
            // Caută postarea în baza de date, nu în memorie
            Post post = postRepository.findById(id).orElse(null);
            if (post == null) {
                LoggerFacade.error("PUT /api/posts failed - Post not found: " + id);
                return createErrorResponse("Post not found", HttpStatus.NOT_FOUND);
            }
            String title = requestMap.get("title");
            String content = requestMap.get("content");
            boolean changed = false;
            if (title != null) {
                if (title.trim().isEmpty() || title.length() < 3 || title.length() > 300) {
                    LoggerFacade.error("PUT /api/posts failed - Title validation failed");
                    return createErrorResponse("Title must be between 3 and 300 characters", HttpStatus.BAD_REQUEST);
                }
                post.setTitle(title);
                changed = true;
            }
            if (content != null) {
                if (content.length() > 10000) {
                    LoggerFacade.error("PUT /api/posts failed - Content too long");
                    return createErrorResponse("Content must not exceed 10000 characters", HttpStatus.BAD_REQUEST);
                }
                post.setContent(content);
                changed = true;
            }
            if (!changed) {
                LoggerFacade.info("PUT /api/posts - No fields to update");
                return createErrorResponse("No fields to update", HttpStatus.BAD_REQUEST);
            }
            postRepository.save(post);
            LoggerFacade.info("PUT /api/posts - Post updated: " + id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", post);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoggerFacade.fatal("PUT /api/posts failed: " + e.getMessage());
            return createErrorResponse("Failed to update post: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to create error responses
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    // DTO class for request body
    public static class PostCreateRequest {
        private String title;
        private String content;
        private String author;
        private String subreddit;

        // Constructors
        public PostCreateRequest() {}

        public PostCreateRequest(String title, String content, String author, String subreddit) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.subreddit = subreddit;
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getSubreddit() {
            return subreddit;
        }

        public void setSubreddit(String subreddit) {
            this.subreddit = subreddit;
        }
    }
}
