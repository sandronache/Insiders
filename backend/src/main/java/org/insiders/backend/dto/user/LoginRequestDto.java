package org.insiders.backend.dto.user;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank
        String email,

        @NotBlank
        String password
) {
}
