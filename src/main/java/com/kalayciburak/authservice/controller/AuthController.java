package com.kalayciburak.authservice.controller;

import com.kalayciburak.authservice.model.dto.request.LoginRequest;
import com.kalayciburak.authservice.model.dto.request.RegisterRequest;
import com.kalayciburak.authservice.service.AuthService;
import com.kalayciburak.authservice.service.EmailVerificationService;
import com.kalayciburak.authservice.service.UserService;
import com.kalayciburak.commonpackage.core.response.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.kalayciburak.commonpackage.core.response.builder.ResponseBuilder.createSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth", produces = "application/json")
@Tag(name = "Kimlik Doğrulama & Token Yönetimi", description = "Kimlik doğrulama ve token yönetimi işlemleri")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;
    private final EmailVerificationService verificationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Yeni bir kullanıcı kaydı oluştur", description = "Tüm kullanıcılar tarafından erişilebilir. Yeni bir hesap oluşturur ve email doğrulama linki gönderir.")
    public Response register(@RequestBody @Valid RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Giriş yap ve access token al", description = "Email ve şifre ile kimlik doğrulaması yapar ve Access/Refresh token üretir. Email doğrulaması gereklidir.")
    public Response login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış yap", description = "Token'ı karaliste ekleyerek kullanıcıyı sistemden çıkarır.")
    public Response logout(@RequestParam String token) {
        return authService.logout(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access token yenile", description = "Refresh token kullanarak yeni bir erişim tokenı alır.")
    public Response refresh(@RequestParam String refreshToken) {
        return authService.refresh(refreshToken);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Email adresini doğrula", description = "Email'e gönderilen doğrulama linkindeki token ile email adresini doğrular.")
    public Response verifyEmail(@RequestParam String token) {
        verificationService.verifyEmail(token);

        return createSuccessResponse("Email adresiniz başarıyla doğrulandı!");
    }

    @PostMapping("/resend-verification-email")
    @Operation(summary = "Doğrulama emailini tekrar gönder", description = "Email doğrulama linkini tekrar gönderir.")
    public Response resendVerificationEmail(@RequestParam @Email String email) {
        verificationService.resendVerificationEmail(email);

        return createSuccessResponse("Doğrulama emaili tekrar gönderildi.");
    }
}