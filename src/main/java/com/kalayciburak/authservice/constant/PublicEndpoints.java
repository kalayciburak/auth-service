package com.kalayciburak.authservice.constant;

public final class PublicEndpoints {
    public static final String[] ENDPOINTS = {
            "/public/**",
            "/v2/api-docs",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/api/auth/**",
            "/.well-known/jwks.json",
            "/api/auth/verify-email",
            "/api/auth/resend-verification-email"
    };

    private PublicEndpoints() {
    }
}