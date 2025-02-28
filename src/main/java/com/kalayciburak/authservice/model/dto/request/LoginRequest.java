package com.kalayciburak.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Sisteme giriş yapabilmek için kullanılan request sınıfıdır.
 *
 * @param username Kullanıcı adı
 * @param password Parola
 */
public record LoginRequest(
        @NotBlank(message = "Kullanıcı adı boş bırakılamaz")
        String username,
        @NotBlank(message = "Parola boş bırakılamaz")
        String password
) {
}
