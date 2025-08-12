package com.insiders.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insiders.dto.ResponseApi;
import com.insiders.dto.comment.CommentCreateRequestDto;
import com.insiders.dto.comment.CommentResponseDto;
import com.insiders.dto.post.PostCreateRequestDto;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.post.PostUpdateRequestDto;
import com.insiders.dto.vote.VoteRequestDto;
import com.insiders.dto.vote.VoteResponseDto;
import com.insiders.http.ApiClient;
import com.insiders.http.ApiResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PostClient {
    private final ApiClient api;

    public PostClient(String baseUrl, Supplier<Map<String,String>> headers) {
        this.api = new ApiClient(baseUrl, new ObjectMapper(), headers);
    }

    public ApiResult<PostResponseDto> getPostById(UUID postId) {
        return api.get("/posts/" + postId, new TypeReference<ResponseApi<PostResponseDto>>(){});
    }

    public ApiResult<List<PostResponseDto>> getAllPosts() {
        return api.get("/posts", new TypeReference<ResponseApi<List<PostResponseDto>>>(){});
    }

    public ApiResult<PostResponseDto> createPost(PostCreateRequestDto createRequest) {
        return api.post("/posts", createRequest, new TypeReference<ResponseApi<PostResponseDto>>(){});
    }

    public ApiResult<PostResponseDto> updatePost(UUID postId, PostUpdateRequestDto updateRequest) {
        return api.put("/posts/" + postId, updateRequest, new TypeReference<ResponseApi<PostResponseDto>>(){});
    }

    public ApiResult<String> deletePost(UUID postId) {
        return api.delete("/posts/" + postId, new TypeReference<ResponseApi<String>>(){});
    }

    public ApiResult<VoteResponseDto> votePost(UUID postId, String voteType) {
        VoteRequestDto voteRequest = new VoteRequestDto(voteType);
        return api.put("/posts/" + postId + "/vote", voteRequest, new TypeReference<ResponseApi<VoteResponseDto>>(){});
    }

    public ApiResult<VoteResponseDto> upvotePost(UUID postId) {
        return votePost(postId, "UPVOTE");
    }

    public ApiResult<VoteResponseDto> downvotePost(UUID postId) {
        return votePost(postId, "DOWNVOTE");
    }

    public ApiResult<List<CommentResponseDto>> getCommentsForPost(UUID postId) {
        return api.get("/posts/" + postId + "/comments",
                new TypeReference<ResponseApi<List<CommentResponseDto>>>(){});
    }

    public ApiResult<CommentResponseDto> createComment(UUID postId, CommentCreateRequestDto commentRequest) {
        return api.post("/posts/" + postId + "/comments", commentRequest,
                new TypeReference<ResponseApi<CommentResponseDto>>(){});
    }

    public ApiResult<VoteResponseDto> voteComment(UUID commentId, String voteType) {
        VoteRequestDto voteRequest = new VoteRequestDto(voteType);
        return api.put("/comments/" + commentId + "/vote", voteRequest, new TypeReference<ResponseApi<VoteResponseDto>>(){});
    }

    public ApiResult<VoteResponseDto> upvoteComment(UUID commentId) {
        return voteComment(commentId, "UPVOTE");
    }

    public ApiResult<VoteResponseDto> downvoteComment(UUID commentId) {
        return voteComment(commentId, "DOWNVOTE");
    }
}
