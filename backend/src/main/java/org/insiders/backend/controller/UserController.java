package org.insiders.backend.controller;

import jakarta.validation.Valid;
import org.insiders.backend.dto.user.LoginRequestDto;
import org.insiders.backend.dto.user.LoginResponseDto;
import org.insiders.backend.dto.user.UserCreateRequestDto;
import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.logger.AsyncLogManager;
import org.insiders.backend.service.IAuthService;
import org.insiders.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userManagementService;
    private final IAuthService authService;
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    @Autowired
    public UserController(IUserService userManagementService, IAuthService authService) {
        this.userManagementService = userManagementService;
        this.authService = authService;
        logger.log("INFO", "UserController initialized with endpoints:");
        logger.log("INFO", "- POST /users");
        logger.log("INFO", "- GET /users/{userId}");
        logger.log("INFO", "- GET /users");
        logger.log("INFO", "- POST /users/login");
    }

    @PostMapping()
    public ResponseEntity<ResponseApi<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        try {
            logger.log("INFO", "POST request received to create user with username: " + request.username());

            UserResponseDto response = userManagementService.saveUser(request.username(), request.email(), request.password());

            logger.log("INFO", "Successfully created user with username: " + request.username());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to create user: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseApi<UserResponseDto>> getUser(@PathVariable UUID userId) {
        try {
            logger.log("INFO", "GET request received for user ID: " + userId);

            UserResponseDto response = userManagementService.getUserById(userId);

            logger.log("INFO", "Successfully retrieved user with ID: " + userId);
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve user: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<UserResponseDto>>> getUsers() {
        try {
            logger.log("INFO", "GET request received for all users");

            List<UserResponseDto> users = userManagementService.getAllUsers();

            logger.log("INFO", "Successfully retrieved " + users.size() + " users");
            return ResponseEntity.ok(new ResponseApi<>(true, users));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve users: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            // Remove reference to username() which doesn't exist
            logger.log("INFO", "POST request received for login");

            LoginResponseDto response = authService.login(request);

            // Remove reference to username() which doesn't exist
            logger.log("INFO", "Successfully logged in user");
            return ResponseEntity.ok(new ResponseApi<>(true, response));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to log in user: " + e.getMessage());
            throw e;
        }
    }

}
