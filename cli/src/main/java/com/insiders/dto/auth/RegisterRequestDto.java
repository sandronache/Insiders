package com.insiders.dto.auth;

public record RegisterRequestDto(
        String username,
        String email,
        String password
) {
}
