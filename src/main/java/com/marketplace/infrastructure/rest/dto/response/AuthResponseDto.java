package com.marketplace.infrastructure.rest.dto.response;

public record AuthResponseDto(
        String token,
        String tokenType
) {
    public AuthResponseDto(String token) {
        this(token, "Bearer");
    }
}
