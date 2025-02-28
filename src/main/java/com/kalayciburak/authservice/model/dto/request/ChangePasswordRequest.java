package com.kalayciburak.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.kalayciburak.authservice.constant.Regexp.PASSWORD_PATTERN;

public record ChangePasswordRequest(
        @NotBlank(message = "Eski parola boş bırakılamaz")
        String oldPassword,

        @NotBlank(message = "Yeni parola boş bırakılamaz")
        @Size(min = 6, max = 30, message = "Parola en az 6, en fazla 30 karakter olabilir")
        @Pattern(regexp = PASSWORD_PATTERN, message = "Parola en az bir harf ve bir rakam içermelidir")
        String newPassword
) {
}
