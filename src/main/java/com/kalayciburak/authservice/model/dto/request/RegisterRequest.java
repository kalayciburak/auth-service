package com.kalayciburak.authservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.kalayciburak.authservice.constant.Regexp.PASSWORD_PATTERN;

/**
 * Sisteme kayıt olabilmek için kullanılan request sınıfıdır.
 *
 * @param firstName İsim
 * @param lastName  Soyisim
 * @param email     Email adresi
 * @param password  Parola
 */
public record RegisterRequest(
        @NotBlank(message = "İsim boş bırakılamaz")
        @Size(min = 2, max = 50, message = "İsim en az 2, en fazla 50 karakter olabilir")
        String firstName,

        @NotBlank(message = "Soyisim boş bırakılamaz")
        @Size(min = 2, max = 50, message = "Soyisim en az 2, en fazla 50 karakter olabilir")
        String lastName,

        @NotBlank(message = "Email boş bırakılamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        String email,

        @NotBlank(message = "Parola boş bırakılamaz")
        @Size(min = 6, max = 30, message = "Parola en az 6, en fazla 30 karakter olabilir")
        @Pattern(regexp = PASSWORD_PATTERN, message = "Parola en az bir harf ve bir rakam içermelidir")
        String password
) {
}
