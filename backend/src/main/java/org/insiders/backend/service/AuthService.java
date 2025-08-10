package org.insiders.backend.service;

import org.insiders.backend.dto.user.LoginRequestDto;
import org.insiders.backend.dto.user.LoginResponseDto;
import org.insiders.backend.entity.User;
import org.insiders.backend.exceptions.UnauthorizedException;
import org.insiders.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService{
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request){
        String email= request.email();
        String password = request.password();

        User user = userRepository.findByEmail(email).orElseThrow(()-> new UnauthorizedException("Email sau parola invalida"));

        if(user.getHashedPassword()!=password.hashCode()){
            throw new UnauthorizedException("Email sau parola invalida");
        }

        return new LoginResponseDto(user.getId(),user.getUsername());
    }
}
