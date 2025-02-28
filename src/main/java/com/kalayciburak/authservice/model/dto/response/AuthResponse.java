package com.kalayciburak.authservice.model.dto.response;

public record AuthResponse(
        String token,
        String refreshToken
) {
}
