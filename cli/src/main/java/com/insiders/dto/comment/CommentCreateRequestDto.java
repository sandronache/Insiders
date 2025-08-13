package com.insiders.dto.comment;

import java.util.UUID;

public record CommentCreateRequestDto(
        String content,
        String author,
        UUID parentId
) {
}
