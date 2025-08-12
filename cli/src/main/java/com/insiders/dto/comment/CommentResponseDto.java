package com.insiders.dto.comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDto(
        UUID id,
        String content,
        String author,
        UUID postId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
