package main.java.controller;

import jakarta.validation.Valid;
import main.java.dto.post.PostCreateRequestDto;
import main.java.dto.post.PostResponseDto;
import main.java.dto.post.PostUpdateRequestDto;
import main.java.logger.LoggerFacade;
import main.java.mapper.PostMapper;
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
@RequestMapping("/posts")
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
        LoggerFacade.info("- GET /posts");
        LoggerFacade.info("- POST /posts");
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getAllPosts(@RequestParam(required = false) String subreddit) {
        LoggerFacade.info("GET /posts called");

        List<Post> posts = postManagementService.getAllPosts(subreddit);
        List<PostResponseDto> dtos = posts.stream().map(PostMapper::postToDto).toList();
        Map<String, Object> response = new HashMap<>();
        response.put("success",true);
        response.put("data",dtos);

        return ResponseEntity.ok(response);

    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(@Valid @RequestBody PostCreateRequestDto requestDto) {
        Post post = postManagementService.createPost(
                requestDto.title(),
                requestDto.content(),
                requestDto.author(),
                requestDto.subreddit()
        );

        PostResponseDto dto = PostMapper.postToDto(post);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dto);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable UUID id,
                                        @Valid @RequestBody PostUpdateRequestDto requestDto) {
        PostResponseDto response = postManagementService.updatePost(id, requestDto);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable UUID postId) {
        postManagementService.deletePostById(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Postarea a fost stearsa cu succes");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable UUID postId) {
        Post post = postManagementService.getPostById(postId);
        PostResponseDto dto = PostMapper.postToDto(post);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dto);

        return ResponseEntity.ok(response);
    }


}
