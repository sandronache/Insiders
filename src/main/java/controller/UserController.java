package main.java.controller;

import jakarta.validation.Valid;
import main.java.dto.user.UserCreateRequestDto;
import main.java.dto.user.UserResponseDto;
import main.java.entity.User;
import main.java.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping()
    public ResponseEntity<ResponseApi<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto request){
        UserResponseDto response = userManagementService.saveUser(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseApi<>(true, response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseApi<UserResponseDto>> getUser(@PathVariable UUID userId){
        UserResponseDto response = userManagementService.getUserById(userId);
        return ResponseEntity.ok(new ResponseApi<>(true, response));
    }

    @GetMapping()
    public ResponseEntity<ResponseApi<List<UserResponseDto>>> getUsers(){
        List<UserResponseDto> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(new ResponseApi<>(true, users));
    }

}
