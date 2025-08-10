package org.insiders.backend.dto.post;

import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(
        @Size(min = 3, max = 300)
        String title,

        @Size(max = 10000)
        String content
) {
}
