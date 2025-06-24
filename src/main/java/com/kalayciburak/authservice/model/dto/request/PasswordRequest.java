package com.kalayciburak.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static com.kalayciburak.authservice.constant.Regexp.PASSWORD_PATTERN;

public record PasswordRequest(
        @NotBlank(message = "Bu alan boş bırakılamaz")
        @Size(min = 8, max = 50, message = "Parola en az 8, en fazla 50 karakter olabilir")
        @Pattern(regexp = PASSWORD_PATTERN, message = "Parola en az bir küçük harf, bir büyük harf, bir rakam ve bir özel karakter (!@#$%^&*()_+) içermelidir")
        String password
) {
}
