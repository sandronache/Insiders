package com.insiders.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insiders.dto.ResponseApi;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.subreddit.SubredditCreateRequestDto;
import com.insiders.dto.subreddit.SubredditResponseDto;
import com.insiders.http.ApiClient;
import com.insiders.http.ApiResult;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SubredditClient {
    private final ApiClient api;

    public SubredditClient(String baseUrl, Supplier<Map<String,String>> headers) {
        this.api = new ApiClient(baseUrl, new ObjectMapper(), headers);
    }

    public ApiResult<List<SubredditResponseDto>> getAllSubreddits() {
        return api.get("/subreddits", new TypeReference<ResponseApi<List<SubredditResponseDto>>>(){});
    }

    public ApiResult<SubredditResponseDto> getSubredditByName(String name) {
        return api.get("/subreddits/" + name, new TypeReference<ResponseApi<SubredditResponseDto>>(){});
    }

    public ApiResult<SubredditResponseDto> createSubreddit(SubredditCreateRequestDto createRequest) {
        return api.post("/subreddits", createRequest, new TypeReference<ResponseApi<SubredditResponseDto>>(){});
    }

    public ApiResult<List<PostResponseDto>> getSubredditPosts(String subredditName) {
        return api.get("/subreddits/" + subredditName + "/posts", new TypeReference<ResponseApi<List<PostResponseDto>>>(){});
    }
}
