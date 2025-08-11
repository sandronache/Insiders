package com.insiders.dto.auth;

import java.util.UUID;

public record LoginResponseDto(
        UUID userId,
        String username
) {
}
