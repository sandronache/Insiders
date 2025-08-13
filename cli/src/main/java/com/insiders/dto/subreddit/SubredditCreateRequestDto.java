package com.insiders.dto.subreddit;

public record SubredditCreateRequestDto(
        String name,
        String displayName,
        String description,
        String iconUrl
) {
}
