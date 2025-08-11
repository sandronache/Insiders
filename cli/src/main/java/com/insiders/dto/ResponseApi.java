package com.insiders.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseApi<T>(
        boolean success,
        T data,
        String message,
        Long total,
        ErrorBody error
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record ErrorBody(String code, String message, Object details) {}
}
