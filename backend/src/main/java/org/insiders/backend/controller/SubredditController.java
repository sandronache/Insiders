package org.insiders.backend.controller;


import jakarta.validation.Valid;
import org.insiders.backend.dto.subreddit.SubredditCreateRequestDto;
import org.insiders.backend.dto.subreddit.SubredditResponseDto;
import org.insiders.backend.dto.subreddit.SubredditUpdateRequestDto;
import org.insiders.backend.logger.AsyncLogManager;
import org.insiders.backend.model.PostModel;
import org.insiders.backend.service.PostManagementService;
import org.insiders.backend.service.SubredditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subreddits")
public class SubredditController {
    private final SubredditService subredditService;
    private final PostManagementService postManagementService;
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    @Autowired
    public SubredditController(SubredditService subredditService, PostManagementService postManagementService) {
        this.subredditService = subredditService;
        this.postManagementService = postManagementService;
        logger.log("INFO", "SubredditController initialized with endpoints:");
        logger.log("INFO", "- GET /subreddits");
        logger.log("INFO", "- GET /subreddits/{name}");
        logger.log("INFO", "- POST /subreddits");
        logger.log("INFO", "- GET /subreddits/{name}/posts");
        logger.log("INFO", "- PUT /subreddits/{name}");
        logger.log("INFO", "- DELETE /subreddits/{name}");
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<SubredditResponseDto>>> getAllSubreddits() {
        try {
            logger.log("INFO", "GET request received for all subreddits");

            List<SubredditResponseDto> response = subredditService.getAllSubreddits();

            logger.log("INFO", "Successfully retrieved " + response.size() + " subreddits");
            return ResponseEntity.ok(new ResponseApi<>(true, response, response.size()));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve subreddits: " + e.getMessage());
            throw e;
        }
    }


    @GetMapping("/{name}")
    public ResponseEntity<ResponseApi<SubredditResponseDto>> getSubreddit(@PathVariable String name) {
        try {
            logger.log("INFO", "GET request received for subreddit: " + name);

            SubredditResponseDto response = subredditService.getSubredditByName(name);

            logger.log("INFO", "Successfully retrieved subreddit: " + name);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve subreddit: " + e.getMessage());
            throw e;
        }
    }


    @PostMapping()
    public ResponseEntity<ResponseApi<SubredditResponseDto>> createSubreddit(@Valid @RequestBody SubredditCreateRequestDto request) {
        try {
            logger.log("INFO", "POST request received to create new subreddit");

            SubredditResponseDto response = subredditService.createSubreddit(request);

            logger.log("INFO", "Successfully created subreddit: " + response.name());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to create subreddit: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{name}/posts")
    public ResponseEntity<ResponseApi<List<PostModel>>> getPostsFromSubreddit(@PathVariable String name,
                                                                              @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "GET request received for posts in subreddit: " + name + " by user: " + username);

            List<PostModel> response = postManagementService.getAllPosts(name, username);

            logger.log("INFO", "Successfully retrieved " + response.size() + " posts from subreddit: " + name);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve posts from subreddit: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{name}")
    public ResponseEntity<ResponseApi<SubredditResponseDto>> updateSubreddit(@PathVariable String name, @Valid @RequestBody SubredditUpdateRequestDto request) {
        try {
            logger.log("INFO", "PUT request received to update subreddit: " + name);

            SubredditResponseDto response = subredditService.update(name, request);

            logger.log("INFO", "Successfully updated subreddit: " + name);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to update subreddit: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ResponseApi<String>> deleteSubreddit(@PathVariable String name) {
        try {
            logger.log("INFO", "DELETE request received for subreddit: " + name);

            subredditService.delete(name);

            logger.log("INFO", "Successfully deleted subreddit: " + name);
            return ResponseEntity.ok(new ResponseApi<>(true, "Subreddit sters cu succes!"));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to delete subreddit: " + e.getMessage());
            throw e;
        }
    }

}
