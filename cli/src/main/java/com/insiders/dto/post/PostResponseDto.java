package com.insiders.dto.post;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDto(
        UUID id,
        String title,
        String content,
        String author,
        String subreddit,
        int upvotes,
        int downvotes,
        int score,
        int commentCount,
        String userVote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
