package org.insiders.backend.controller;

import jakarta.validation.Valid;
import org.insiders.backend.dto.user.LoginRequestDto;
import org.insiders.backend.dto.user.LoginResponseDto;
import org.insiders.backend.dto.user.UserCreateRequestDto;
import org.insiders.backend.dto.user.UserResponseDto;
import org.insiders.backend.service.AuthService;
import org.insiders.backend.service.IAuthService;
import org.insiders.backend.service.IUserService;
import org.insiders.backend.service.UserManagementService;
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

    @Autowired
    public UserController(IUserService userManagementService, IAuthService authService) {
        this.userManagementService = userManagementService;
        this.authService = authService;
    }

    @PostMapping()
    public ResponseEntity<ResponseApi<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto request) {
        UserResponseDto response = userManagementService.saveUser(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseApi<UserResponseDto>> getUser(@PathVariable UUID userId) {
        UserResponseDto response = userManagementService.getUserById(userId);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<UserResponseDto>>> getUsers() {
        List<UserResponseDto> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(new ResponseApi<>(true, users));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseApi<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

}
