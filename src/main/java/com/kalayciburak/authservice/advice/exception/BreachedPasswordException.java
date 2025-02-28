package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BreachedPasswordException extends CompromisedPasswordException {
    public BreachedPasswordException() {
        super("Bu parola veri ihlallerinde tespit edilmiştir. Güvenliğiniz için lütfen başka bir parola belirleyiniz.");
    }

    public BreachedPasswordException(String message) {
        super(message);
    }

    public BreachedPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}