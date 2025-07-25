package main.java.controller;

import main.java.model.Post;
import main.java.repository.PostRepository;
import main.java.service.PostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;
    private final PostManagementService postManagementService;

    @Autowired
    public PostController(PostRepository postRepository, PostManagementService postManagementService) {
        this.postRepository = postRepository;
        this.postManagementService = postManagementService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllOrderedByDate();
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        postRepository.save(post);
        return post;
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Integer postId) {
        postRepository.deleteById(postId);
    }

    @GetMapping("/{postId}")
    public Post getPostById(@PathVariable Integer postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElse(null);
    }
}
