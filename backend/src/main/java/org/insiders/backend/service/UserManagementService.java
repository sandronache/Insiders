package org.insiders.backend.service;

import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.exceptions.UnauthorizedException;
import org.insiders.backend.mapper.UserMapper;
import org.insiders.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for user management operations
 */

@Service
public class UserManagementService {
    private final UserRepository userRepository;

    @Autowired
    private UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Utilizatorul" + username + "nu a fost gasit"));
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilizatorul cu id= " + userId + " nu a fost gasit"));
    }

    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilizatorul cu id= " + userId + " nu a fost gasit"));
        return UserMapper.toDto(user);
    }

    public UserResponseDto saveUser(String username, String email, String password) {
        int hashedPassword = password.hashCode();
        User savedUser = userRepository.save(new User(username, email, hashedPassword));
        return UserMapper.toDto(savedUser);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDto).toList();
    }
}

