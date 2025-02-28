package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.TokenTypeMismatchException;
import com.kalayciburak.authservice.model.dto.request.LoginRequest;
import com.kalayciburak.authservice.model.dto.response.AuthResponse;
import com.kalayciburak.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static com.kalayciburak.authservice.constant.JwtConstants.REFRESH_TOKEN_TYPE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Kullanıcıyı doğrular ve access/refresh token üretir.
     *
     * @param request Kullanıcı giriş bilgileri
     * @return AuthResponse DTO'su içinde token bilgileri
     */
    public ResponseEntity<AuthResponse> login(LoginRequest request) {
        authenticateUser(request.username(), request.password());

        return ResponseEntity.ok(generateAuthTokens(request.username()));
    }

    /**
     * Refresh token ile yeni access ve refresh token üretir.
     *
     * @param refreshToken Kullanıcının mevcut refresh token'ı
     * @return Yeni üretilmiş access ve refresh token'ları içeren AuthResponse
     */
    public ResponseEntity<AuthResponse> refresh(String refreshToken) {
        validateRefreshToken(refreshToken);
        var username = jwtUtil.extractUsername(refreshToken);

        return ResponseEntity.ok(generateAuthTokens(username));
    }

    /**
     * Kullanıcı adı ve şifre ile kimlik doğrulaması yapar.
     *
     * @param username Kullanıcı adı
     * @param password Kullanıcı şifresi
     */
    private void authenticateUser(String username, String password) {
        var authToken = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(authToken);
    }

    /**
     * Kullanıcı için access ve refresh token üretir.
     *
     * @param username Kullanıcı adı
     * @return AuthResponse içinde yeni token bilgileri
     */
    private AuthResponse generateAuthTokens(String username) {
        var authorities = getUserAuthorities(username);
        var token = jwtUtil.generateToken(username, authorities);
        var refreshToken = jwtUtil.generateRefreshToken(username);

        return new AuthResponse(token, refreshToken);
    }

    /**
     * Refresh token'ı doğrular ve token tipi geçerli olup olmadığını kontrol eder.
     *
     * @param refreshToken Kullanıcının gönderdiği refresh token
     */
    private void validateRefreshToken(String refreshToken) {
        jwtUtil.validateToken(refreshToken);
        boolean isInvalidTokenType = !REFRESH_TOKEN_TYPE.equals(jwtUtil.getTokenType(refreshToken));
        if (isInvalidTokenType) throw new TokenTypeMismatchException(refreshToken);
    }

    /**
     * Kullanıcının yetkilerini getirir.
     *
     * @param username Kullanıcı adı
     * @return Kullanıcının sahip olduğu yetki listesi
     */
    private Collection<? extends GrantedAuthority> getUserAuthorities(String username) {
        return customUserDetailsService.loadUserByUsername(username).getAuthorities();
    }
}
