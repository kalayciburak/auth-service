package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Email adresiniz henüz doğrulanmamış. Lütfen email adresinizi doğrulayın.");
    }

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}