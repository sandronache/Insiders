package main.java.dto.subreddit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubredditCreateRequestDto(
        @NotBlank(message = "Numele subredditului este obligatoriu")
        @Size(min = 3, max = 50, message = "Numele subredditului trebuie sa aiba intre 3 si 50 de caractere")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Numele subredditului poate contine doar caractere alfanumerice si underscore")
        String name,

        @NotBlank(message = "displayName este obligatoriu")
        @Size(min = 3, max= 100, message = "displayName trebuie sa aiba intre 3 si 100 de caractere")
        String displayName,

        @NotBlank
        @Size(max = 500, message = "Descrierea trebuie sa aiba maxim 500 de caractere")
        String description,

        String iconUrl
) {
}
