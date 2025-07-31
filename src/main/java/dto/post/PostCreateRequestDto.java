package main.java.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequestDto(
        @NotBlank(message = "Titlul este obligatoriu")
        @Size(min = 3, max = 300)
        String title,

        @Size(max = 10000)
        String content,

        @NotBlank(message = "Autorul este obligatoriu")
        String author,

        @NotBlank(message = "Subreddit-ul este obligatoriu")
        String subreddit
) {
}
