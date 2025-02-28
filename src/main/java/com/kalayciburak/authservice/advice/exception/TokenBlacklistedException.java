package com.kalayciburak.authservice.advice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenBlacklistedException extends RuntimeException {
    public TokenBlacklistedException() {
        super("Bu token kara listeye alınmış.");
    }

    public TokenBlacklistedException(String token) {
        super("Bu token kara listeye alınmış: " + token);
    }
}
