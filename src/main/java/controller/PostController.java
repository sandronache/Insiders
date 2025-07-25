package main.java.controller;

import main.java.model.Post;
import main.java.service.AppDataService;
import main.java.service.PostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostManagementService postManagementService;
    private final AppDataService appDataService;

    @Autowired
    public PostController(PostManagementService postManagementService, AppDataService appDataService) {
        this.postManagementService = postManagementService;
        this.appDataService = appDataService;
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return appDataService.getAppData().getLoadedPosts().values().stream().toList();
    }

    @PostMapping
    public String createPost(@RequestParam String content) {
        if (appDataService.getAppData().getLoggedUser() == null) {
            return "No user logged in";
        }
        postManagementService.addPost(appDataService.getAppData(), content);
        return "Post created successfully";
    }

    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Integer postId) {
        if (appDataService.getAppData().getLoggedUser() == null) {
            return "No user logged in";
        }
        postManagementService.deletePost(appDataService.getAppData(), postId);
        return "Post deleted successfully";
    }

    @GetMapping("/{postId}")
    public Post getPostById(@PathVariable Integer postId) {
        return appDataService.getAppData().getLoadedPosts().get(postId);
    }
}
