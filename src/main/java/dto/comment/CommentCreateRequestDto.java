package main.java.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentCreateRequestDto(
        @NotBlank(message = "Continutul nu poate fi gol")
        @Size(max = 1000)
        String content,

        @NotBlank(message = "Username-ul autorului este obligatoriu")
        String author,

        UUID parentId
) {
}
