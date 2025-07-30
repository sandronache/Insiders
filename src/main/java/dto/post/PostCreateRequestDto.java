package main.java.dto.post;

public record PostCreateRequestDto(
        String title,
        String content,
        String author,
        String subreddit
) {
}
