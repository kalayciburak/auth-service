package com.kalayciburak.authservice.controller;

import com.kalayciburak.authservice.model.dto.request.LoginRequest;
import com.kalayciburak.authservice.model.dto.request.RegisterRequest;
import com.kalayciburak.authservice.model.dto.response.AuthResponse;
import com.kalayciburak.authservice.model.dto.response.UserResponse;
import com.kalayciburak.authservice.service.AuthService;
import com.kalayciburak.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth", produces = "application/json")
@Tag(name = "Kimlik Doğrulama & Token Yönetimi", description = "Kimlik doğrulama ve token yönetimi işlemleri")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Yeni bir kullanıcı kaydı oluştur",
            description = "Tüm kullanıcılar tarafından erişilebilir. Yeni bir hesap oluşturur."
    )
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Giriş yap ve access token al",
            description = "Kullanıcı adı ve şifre ile kimlik doğrulaması yapar ve Access/Refresh token üretir."
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Çıkış yap",
            description = "Token'ı karaliste ekleyerek kullanıcıyı sistemden çıkarır."
    )
    public ResponseEntity<String> logout(@RequestParam String token) {
        return authService.logout(token);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Access token yenile",
            description = "Refresh token kullanarak yeni bir erişim tokenı alır."
    )
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }
}