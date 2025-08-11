package com.insiders.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insiders.dto.ResponseApi;
import com.insiders.dto.auth.LoginRequestDto;
import com.insiders.dto.auth.LoginResponseDto;
import com.insiders.dto.auth.RegisterRequestDto;
import com.insiders.dto.auth.RegisterResponseDto;
import com.insiders.http.ApiClient;
import com.insiders.http.ApiResult;

import java.util.Map;
import java.util.function.Supplier;

public class AuthClient {
    private final ApiClient api;

    public AuthClient(String baseUrl, Supplier<Map<String,String>> headers) {
        this.api = new ApiClient(baseUrl, new ObjectMapper(), headers);
    }

    public ApiResult<RegisterResponseDto> register(RegisterRequestDto req) {
        return api.post("/users", req, new TypeReference<ResponseApi<RegisterResponseDto>>(){});
    }

    public ApiResult<LoginResponseDto> login(LoginRequestDto req) {
        return api.post("/users/login", req, new TypeReference<ResponseApi<LoginResponseDto>>(){});
    }
}
