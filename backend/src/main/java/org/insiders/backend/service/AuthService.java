package org.insiders.backend.service;

import org.insiders.backend.dto.user.LoginRequestDto;
import org.insiders.backend.dto.user.LoginResponseDto;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.UnauthorizedException;
import org.insiders.backend.logger.AsyncLogManager;
import org.insiders.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService{
    private final UserRepository userRepository;
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        try {
            String email = request.email();
            String password = request.password();

            logger.log("INFO", "Login attempt received for email: " + email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.log("WARN", "Login failed: User not found with email: " + email);
                        return new UnauthorizedException("Email sau parola invalida");
                    });

            if (user.getHashedPassword() != password.hashCode()) {
                logger.log("WARN", "Login failed: Invalid password for user: " + email);
                throw new UnauthorizedException("Email sau parola invalida");
            }

            logger.log("INFO", "Login successful for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            return new LoginResponseDto(user.getId(), user.getUsername());
        } catch (UnauthorizedException e) {
            // Already logged above
            throw e;
        } catch (Exception e) {
            logger.log("ERROR", "Unexpected error during login process: " + e.getMessage());
            throw e;
        }
    }
}
