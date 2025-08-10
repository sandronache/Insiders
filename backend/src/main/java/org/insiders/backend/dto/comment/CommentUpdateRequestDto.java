package org.insiders.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequestDto(
        @NotBlank(message = "Continutul nu poate lipsi")
        @Size(max = 1000)
        String content
) {
}
