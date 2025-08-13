package com.insiders.dto.subreddit;

import java.time.Instant;
import java.util.UUID;

public record SubredditResponseDto(
        UUID id,
        String name,
        String displayName,
        String description,
        int memberCount,
        int postCount,
        String iconUrl,
        Instant createdAt
) {
}
