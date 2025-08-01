package main.java.controller;

import jakarta.validation.Valid;
import main.java.dto.comment.CommentCreateRequestDto;
import main.java.dto.comment.CommentResponseDto;
import main.java.dto.post.*;
import main.java.entity.Post;
import main.java.logger.LoggerFacade;
import main.java.mapper.PostMapper;
import main.java.service.CommentService;
import main.java.service.PostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseApi<List<PostResponseDto>>> getAllPosts(@RequestParam(required = false) String subreddit) {
        LoggerFacade.info("GET /posts called");

        List<Post> posts = postManagementService.getAllPosts(subreddit);
        List<PostResponseDto> dtos = posts.stream().map(PostMapper::postToDto).toList();

        return ResponseEntity.ok(new ResponseApi<>(true,dtos));

    }

    @PostMapping
    public ResponseEntity<ResponseApi<PostResponseDto>> createPost(@Valid @RequestBody PostCreateRequestDto requestDto) {
        Post post = postManagementService.createPost(
                requestDto.title(),
                requestDto.content(),
                requestDto.author(),
                requestDto.subreddit()
        );

        PostResponseDto dto = PostMapper.postToDto(post);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, dto));

    }


    @PutMapping("/{id}")
    public ResponseEntity<ResponseApi<PostResponseDto>> updatePost(@PathVariable UUID id,
                                        @Valid @RequestBody PostUpdateRequestDto requestDto) {
        PostResponseDto response = postManagementService.updatePost(id, requestDto);
        return ResponseEntity.ok(new ResponseApi<>(true,response));
    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseApi<String>> deletePost(@PathVariable UUID postId) {
        postManagementService.deletePostById(postId);
        return ResponseEntity.ok(new ResponseApi<>(true, "Postarea a fost stearsa cu succes!"));
    }


    @GetMapping("/{postId}")
    public ResponseEntity<ResponseApi<PostResponseDto>> getPostById(@PathVariable UUID postId) {
        Post post = postManagementService.getPostById(postId);
        PostResponseDto dto = PostMapper.postToDto(post);

        return ResponseEntity.ok(new ResponseApi<>(true,dto));
    }

    @PutMapping("/{postId}/vote")
    public ResponseEntity<ResponseApi<VoteResponseDto>> votePost(@PathVariable UUID postId, @RequestBody VoteRequestDto request){
        VoteResponseDto response = postManagementService.votePost(postId, request.voteType());
        return ResponseEntity.ok(new ResponseApi<>(true,response));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<List<CommentResponseDto>>> getCommentsForPost(@PathVariable UUID postId){
        List<CommentResponseDto> comments = commentService.getCommentsForPost(postId);
        return ResponseEntity.ok(new ResponseApi<>(true,comments,comments.size()));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ResponseApi<CommentResponseDto>> createComment(@PathVariable UUID postId, @RequestBody CommentCreateRequestDto request){
        CommentResponseDto response = commentService.createComment(postId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
    }



}
