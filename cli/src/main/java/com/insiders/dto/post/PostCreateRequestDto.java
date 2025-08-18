package com.insiders.dto.post;

import java.io.File;

public record PostCreateRequestDto(
        String title,
        String content,
        String author,
        String subreddit,
        File image,
        Integer filter
) {
    public PostCreateRequestDto(String title, String content, String author, String subreddit) {
        this(title, content, author, subreddit, null, null);
    }

    public PostCreateRequestDto(String title, String content, String author, String subreddit, File image) {
        this(title, content, author, subreddit, image, null);
    }
}
