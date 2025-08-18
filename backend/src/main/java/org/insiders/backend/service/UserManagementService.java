package org.insiders.backend.service;

import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.NotFoundException;
import org.insiders.backend.mapper.UserMapper;
import org.insiders.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for user management operations
 */

@Service
@Transactional(readOnly = true)
public class UserManagementService implements IUserService {
    private final UserRepository userRepository;

    @Autowired
    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Utilizatorul " + username + " nu a fost gasit"));
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilizatorul cu id= " + userId + " nu a fost gasit"));
    }

    @Override
    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Utilizatorul cu id= " + userId + " nu a fost gasit"));
        return UserMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto saveUser(String username, String email, String password) {
        int hashedPassword = password.hashCode();
        User savedUser = userRepository.saveAndFlush(new User(username, email, hashedPassword));
        return UserMapper.toDto(savedUser);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDto).toList();
    }
}

