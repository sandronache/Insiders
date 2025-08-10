package org.insiders.backend.mapper;

import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.entity.User;

public class UserMapper {
    public static UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
