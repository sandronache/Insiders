package com.insiders.dto.auth;

public record LoginRequestDto(
        String email,
        String password
) {
}
