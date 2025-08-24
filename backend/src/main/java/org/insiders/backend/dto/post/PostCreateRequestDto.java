package org.insiders.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PostCreateRequestDto(
        @NotBlank(message = "Titlul este obligatoriu")
        @Size(min = 3, max = 300)
        String title,

        @Size(max = 10000)
        String content,

        @NotBlank(message = "Autorul este obligatoriu")
        @Size(min = 3, max = 20, message = "Username-ul trebuie sa aiba intre 3 si 20 de caractere")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username-ul poate contine doar caractere alfanumerice si underscore")
        String author,

        @NotBlank(message = "Subreddit-ul este obligatoriu")
        @Size(min = 3, max = 50, message = "Subreddit-ul trebuie sa aiba intre 3 si 50 de caractere")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Subreddit-ul poate contine doar caractere alfanumerice si underscore")
        String subreddit,

        MultipartFile image,
        Integer filterId
) {
}
