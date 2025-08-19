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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostManagementService postManagementService;
    private final CommentService commentService;

    @Autowired
    public PostController(PostManagementService postManagementService, CommentService commentService) {
        this.postManagementService = postManagementService;
        this.commentService = commentService;
        LoggerFacade.info("PostController initialized with endpoints:");
        LoggerFacade.info("- GET /posts");
        LoggerFacade.info("- POST /posts");
        LoggerFacade.info("- PUT /posts/{id}");
        LoggerFacade.info("- DELETE /posts/{id}");
        LoggerFacade.info("- GET /posts/{id}");
        LoggerFacade.info("- PUT /posts/{id}/vote");
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<PostResponseDto>>> getAllPosts(@RequestParam(required = false) String subreddit,
                                                                          @RequestParam(defaultValue = "current_user") String username) {
        LoggerFacade.info("GET /posts called");

        List<PostModel> posts = postManagementService.getAllPosts(subreddit, username);
        List<PostResponseDto> dtos = posts.stream().map(PostMapper::postModelToDto).toList();

        return ResponseEntity.ok(new ResponseApi<>(true, dtos));

    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, headers = "Content-Type=multipart/form-data")
    public ResponseEntity<ResponseApi<PostResponseDto>> createPostMultipart(@RequestParam("title") String title,
                                                                   @RequestParam(value = "content", required = false) String content,
                                                                   @RequestParam("author") String author,
                                                                   @RequestParam("subreddit") String subreddit,
                                                                   @RequestPart(value = "image", required = false) MultipartFile image,
                                                                   @RequestParam(value = "filter", required = false) Integer filter) {

        PostModel post = postManagementService.createPost(
                title,
                content,
                author,
                subreddit,
                image
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, PostMapper.postModelToDto(post)));

    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Content-Type=application/json")
    public ResponseEntity<ResponseApi<PostResponseDto>> createPostJson(@RequestBody PostCreateRequestDto requestDto) {

        PostModel post = postManagementService.createPost(
                requestDto.title(),
                requestDto.content(),
                requestDto.author(),
                requestDto.subreddit(),
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, PostMapper.postModelToDto(post)));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<PostResponseDto>> updatePost(@PathVariable UUID id,
                                                                   @Valid @RequestBody PostUpdateRequestDto requestDto,
                                                                   @RequestParam(defaultValue = "current_user") String username) {
        PostModel postModel = postManagementService.updatePost(id, requestDto, username);
        PostResponseDto response = PostMapper.postModelToDto(postModel);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseApi<String>> deletePost(@PathVariable UUID postId) {
        postManagementService.deletePostById(postId);
        return ResponseEntity.ok(new ResponseApi<>(true, "Postarea a fost stearsa cu succes!"));
    }


    @GetMapping("/{postId}")
    public ResponseEntity<ResponseApi<PostResponseDto>> getPostById(@PathVariable UUID postId,
                                                                    @RequestParam(defaultValue = "current_user") String username) {
        PostModel post = postManagementService.getPostByIdModel(postId, username);
        PostResponseDto dto = PostMapper.postModelToDto(post);

        return ResponseEntity.ok(new ResponseApi<>(true, dto));
    }

    @PutMapping("/{postId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> votePost(@PathVariable UUID postId, @RequestBody VoteRequestDto request,
                                                                 @RequestParam(defaultValue = "current_user") String username) {
        VoteResponseDto response = postManagementService.votePost(postId, request.voteType(), username);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<List<CommentResponseDto>>> getCommentsForPost(@PathVariable UUID postId, @RequestParam(defaultValue = "current_user") String username) {
        List<CommentResponseDto> comments = commentService.getCommentsForPost(postId, username);
        int total = commentService.countCommentsByPostId(postId);
        return ResponseEntity.ok(new ResponseApi<>(true, comments, total));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<CommentResponseDto>> createComment(@PathVariable UUID postId, @RequestBody CommentCreateRequestDto request) {
        CommentResponseDto response = commentService.createComment(postId, request);
        int total = commentService.countCommentsByPostId(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response,total));
    }


}
