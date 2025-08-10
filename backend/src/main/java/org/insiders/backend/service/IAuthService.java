package org.insiders.backend.service;

import org.insiders.backend.dto.user.LoginRequestDto;
import org.insiders.backend.dto.user.LoginResponseDto;

public interface IAuthService {
    LoginResponseDto login(LoginRequestDto request);
}
