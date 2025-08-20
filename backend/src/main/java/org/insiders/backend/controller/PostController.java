package org.insiders.backend.controller;

import jakarta.validation.Valid;
import org.insiders.backend.dto.comment.CommentCreateRequestDto;
import org.insiders.backend.dto.comment.CommentResponseDto;
import org.insiders.backend.dto.post.PostCreateRequestDto;
import org.insiders.backend.dto.post.PostResponseDto;
import org.insiders.backend.dto.post.PostUpdateRequestDto;
import org.insiders.backend.dto.vote.VoteRequestDto;
import org.insiders.backend.dto.vote.VoteResponseDto;
import org.insiders.backend.logger.LoggerFacade;
import org.insiders.backend.mapper.PostMapper;
import org.insiders.backend.model.PostModel;
import org.insiders.backend.service.CommentService;
import org.insiders.backend.service.PostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.insiders.backend.logger.AsyncLogManager;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostManagementService postManagementService;
    private final CommentService commentService;
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    @Autowired
    public PostController(PostManagementService postManagementService, CommentService commentService) {
        this.postManagementService = postManagementService;
        this.commentService = commentService;
        logger.log("INFO", "PostController initialized with endpoints:");
        logger.log("INFO", "- GET /posts");
        logger.log("INFO", "- POST /posts");
        logger.log("INFO", "- PUT /posts/{id}");
        logger.log("INFO", "- DELETE /posts/{id}");
        logger.log("INFO", "- GET /posts/{id}");
        logger.log("INFO", "- PUT /posts/{id}/vote");
        logger.log("INFO", "- GET /posts/{id}/comments");
        logger.log("INFO", "- POST /posts/{id}/comments");
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<PostResponseDto>>> getAllPosts(@RequestParam(required = false) String subreddit,
                                                                          @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "GET request received for all posts" +
                    (subreddit != null ? " in subreddit: " + subreddit : "") +
                    " by user: " + username);

            List<PostModel> posts = postManagementService.getAllPosts(subreddit, username);
            List<PostResponseDto> dtos = posts.stream().map(PostMapper::postModelToDto).toList();

            logger.log("INFO", "Successfully retrieved " + posts.size() + " posts");
            return ResponseEntity.ok(new ResponseApi<>(true, dtos));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve posts: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, headers = "Content-Type=multipart/form-data")
    public ResponseEntity<ResponseApi<PostResponseDto>> createPostMultipart(@RequestParam("title") String title,
                                                                            @RequestParam(value = "content", required = false) String content,
                                                                            @RequestParam("author") String author,
                                                                            @RequestParam("subreddit") String subreddit,
                                                                            @RequestPart(value = "image", required = false) MultipartFile image,
                                                                            @RequestParam(value = "filter", required = false) String filterName) {
        try {
            logger.log("INFO", "POST request received to create new post with image by user: " + author + " in subreddit: " + subreddit);

            PostModel post = postManagementService.createPost(
                    title,
                    content,
                    author,
                    subreddit,
                    image,
                    filterName
            );

            logger.log("INFO", "Successfully created post with ID: " + post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, PostMapper.postModelToDto(post)));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to create post: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Content-Type=application/json")
    public ResponseEntity<ResponseApi<PostResponseDto>> createPostJson(@RequestBody PostCreateRequestDto requestDto) {
        try {
            logger.log("INFO", "POST request received to create new post by user: " + requestDto.author() +
                    " in subreddit: " + requestDto.subreddit());

            PostModel post = postManagementService.createPost(
                    requestDto.title(),
                    requestDto.content(),
                    requestDto.author(),
                    requestDto.subreddit(),
                    requestDto.image(),
                    requestDto.filterName()
            );

            logger.log("INFO", "Successfully created post with ID: " + post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, PostMapper.postModelToDto(post)));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to create post: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<PostResponseDto>> updatePost(@PathVariable UUID id,
                                                                   @Valid @RequestBody PostUpdateRequestDto requestDto,
                                                                   @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "PUT request received to update post ID: " + id + " by user: " + username);

            PostModel postModel = postManagementService.updatePost(id, requestDto, username);
            PostResponseDto response = PostMapper.postModelToDto(postModel);

            logger.log("INFO", "Successfully updated post with ID: " + id);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to update post: " + e.getMessage());
            throw e;
        }
    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseApi<String>> deletePost(@PathVariable UUID postId) {
        try {
            logger.log("INFO", "DELETE request received for post ID: " + postId);

            postManagementService.deletePostById(postId);

            logger.log("INFO", "Successfully deleted post with ID: " + postId);
            return ResponseEntity.ok(new ResponseApi<>(true, "Postarea a fost stearsa cu succes!"));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to delete post: " + e.getMessage());
            throw e;
        }
    }


    @GetMapping("/{postId}")
    public ResponseEntity<ResponseApi<PostResponseDto>> getPostById(@PathVariable UUID postId,
                                                                    @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "GET request received for post ID: " + postId + " by user: " + username);

            PostModel post = postManagementService.getPostByIdModel(postId, username);
            PostResponseDto dto = PostMapper.postModelToDto(post);

            logger.log("INFO", "Successfully retrieved post with ID: " + postId);
            return ResponseEntity.ok(new ResponseApi<>(true, dto));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve post: " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{postId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> votePost(@PathVariable UUID postId, @RequestBody VoteRequestDto request,
                                                                 @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "PUT request received to vote on post ID: " + postId + " by user: " + username +
                    " with vote type: " + request.voteType());

            VoteResponseDto response = postManagementService.votePost(postId, request.voteType(), username);

            logger.log("INFO", "Successfully processed vote on post with ID: " + postId);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to process vote on post: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<List<CommentResponseDto>>> getCommentsForPost(@PathVariable UUID postId,
                                                                                    @RequestParam(defaultValue = "current_user") String username) {
        try {
            logger.log("INFO", "GET request received for comments on post ID: " + postId + " by user: " + username);

            List<CommentResponseDto> comments = commentService.getCommentsForPost(postId, username);
            int total = commentService.countCommentsByPostId(postId);

            logger.log("INFO", "Successfully retrieved " + comments.size() + " comments for post ID: " + postId);
            return ResponseEntity.ok(new ResponseApi<>(true, comments, total));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve comments for post: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<CommentResponseDto>> createComment(@PathVariable UUID postId, @RequestBody CommentCreateRequestDto request) {
        try {
            logger.log("INFO", "POST request received to create comment on post ID: " + postId);
            CommentResponseDto response = commentService.createComment(postId, request);
            int total = commentService.countCommentsByPostId(postId);

            logger.log("INFO", "Successfully created comment on post ID: " + postId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response, total));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to create comment on post: " + e.getMessage());
            throw e;
        }
    }


}
