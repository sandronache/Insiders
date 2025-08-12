package com.insiders.dto.comment;

public record CommentCreateRequestDto(
        String content,
        String author
) {
}
