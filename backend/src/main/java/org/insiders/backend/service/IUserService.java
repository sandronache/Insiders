package org.insiders.backend.service;

import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    UserResponseDto saveUser(String username, String email, String password);
    User findById(UUID id);
    List<UserResponseDto> getAllUsers();
    User findByUsername(String username);
    UserResponseDto getUserById(UUID id);
}
