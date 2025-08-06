package main.java.mapper;

import main.java.dto.user.UserResponseDto;
import main.java.entity.User;

import java.time.LocalDateTime;

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
