package com.insiders.dto.subreddit;

public record SubredditUpdateRequestDto(
        String displayName,
        String description,
        String iconUrl
) {
}
