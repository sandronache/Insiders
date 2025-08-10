package org.insiders.backend.dto.user;

import java.util.UUID;

public record LoginResponseDto(
        UUID userId,
        String username
) {
}
