package main.java.service;

import main.java.dto.user.UserResponseDto;
import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.exceptions.NotFoundException;
import main.java.exceptions.UnauthorizedException;
import main.java.logger.LoggerFacade;
import main.java.mapper.UserMapper;
import main.java.model.AppData;
import main.java.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

