package com.insiders.dto.auth;

import java.util.UUID;

public record RegisterResponseDto(
        UUID id,
        String email,
        String username
) {
}
