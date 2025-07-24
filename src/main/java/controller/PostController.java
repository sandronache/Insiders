package main.java.controller;

import main.java.model.Post;
import main.java.repository.PostRepository;
import main.java.service.PostManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository = new PostRepository();
    private final PostManagementService postManagementService = PostManagementService.getInstance();

    // Get all posts ordered by date
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllOrderedByDate();
    }

    // Create a new post
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        postRepository.save(post);
        return post;
    }

    // Delete a post by its database ID
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Integer postId) {
        postRepository.deleteById(postId);
    }

    // Get a post by its database ID
    @GetMapping("/{postId}")
    public Post getPostById(@PathVariable Integer postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElse(null);
    }
}