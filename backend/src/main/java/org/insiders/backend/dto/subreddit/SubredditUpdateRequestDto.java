package org.insiders.backend.dto.subreddit;

import jakarta.validation.constraints.Size;

public record SubredditUpdateRequestDto(
        @Size(min = 3, max= 100, message = "displayName trebuie sa aiba intre 3 si 100 de caractere")
        String displayName,

        @Size(max = 500, message = "Descrierea trebuie sa aiba maxim 500 de caractere")
        String description,

        String iconUrl
) {
}
