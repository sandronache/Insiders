package org.insiders.backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt
) {
}
