package com.kalayciburak.authservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Sisteme giriş yapabilmek için kullanılan request sınıfıdır.
 *
 * @param email    Email adresi
 * @param password Parola
 */
public record LoginRequest(
        @NotBlank(message = "Email boş bırakılamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        String email,

        @NotBlank(message = "Parola boş bırakılamaz")
        String password
) {
}
