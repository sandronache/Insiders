package org.insiders.backend.controller;


import jakarta.validation.Valid;
import org.insiders.backend.dto.subreddit.SubredditCreateRequestDto;
import org.insiders.backend.dto.subreddit.SubredditResponseDto;
import org.insiders.backend.dto.subreddit.SubredditUpdateRequestDto;
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

    @Autowired
    public SubredditController(SubredditService subredditService, PostManagementService postManagementService) {
        this.subredditService = subredditService;
        this.postManagementService = postManagementService;
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<SubredditResponseDto>>> getAllSubreddits(){
        List<SubredditResponseDto> response = subredditService.getAllSubreddits();
        return ResponseEntity.ok(new ResponseApi<>(true, response, response.size()));
    }


    @GetMapping("/{name}")
    public ResponseEntity<ResponseApi<SubredditResponseDto>> getSubreddit(@PathVariable String name){
        SubredditResponseDto response = subredditService.getSubredditByName(name);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }


    @PostMapping()
    public ResponseEntity<ResponseApi<SubredditResponseDto>> createSubreddit(@Valid @RequestBody SubredditCreateRequestDto request){
        SubredditResponseDto response = subredditService.createSubreddit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
    }

    @GetMapping("/{name}/posts")
    public ResponseEntity<ResponseApi<List<PostModel>>> getPostsFromSubreddit(@PathVariable String name){
        List<PostModel> response = postManagementService.getAllPosts(name);
        return  ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @PutMapping("/{name}")
    public ResponseEntity<ResponseApi<SubredditResponseDto>> updateSubreddit(@PathVariable String name, @Valid @RequestBody SubredditUpdateRequestDto request){
        SubredditResponseDto response = subredditService.update(name,request);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ResponseApi<String>> deleteSubreddit(@PathVariable String name){
        subredditService.delete(name);
        return ResponseEntity.ok(new ResponseApi<>(true, "Subreddit sters cu succes!"));
    }

}
