package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.EmailNotVerifiedException;
import com.kalayciburak.authservice.advice.exception.TokenBlacklistedException;
import com.kalayciburak.authservice.advice.exception.TokenTypeMismatchException;
import com.kalayciburak.authservice.model.dto.request.LoginRequest;
import com.kalayciburak.authservice.model.dto.response.AuthResponse;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.security.token.JwtUtil;
import com.kalayciburak.authservice.security.token.TokenBlacklistService;
import com.kalayciburak.commonpackage.core.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static com.kalayciburak.authservice.constant.JwtConstants.REFRESH_TOKEN_TYPE;
import static com.kalayciburak.commonpackage.core.constant.Messages.Auth.*;
import static com.kalayciburak.commonpackage.core.response.builder.ResponseBuilder.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Kullanıcıyı doğrular ve access token üretir. Email doğrulamasını kontrol eder.
     *
     * @param request Kullanıcı giriş bilgileri
     * @return AuthResponse DTO'su içinde token bilgileri
     */
    public SuccessResponse<AuthResponse> login(LoginRequest request) {
        // Email'e göre kullanıcıyı bul ve email doğrulamasını kontrol et
        var user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!user.isEmailVerified()) throw new EmailNotVerifiedException();

        // Email ile authentication yap
        authenticateUser(request.email(), request.password());
        var response = generateAuthTokens(request.email());

        return createSuccessResponse(response, LOGIN_SUCCESS);
    }

    /**
     * Kullanıcıyı sistemden çıkarır ve token'ı kara listeye alır.
     *
     * @param token Kara listeye alınacak token
     * @return Başarılı bir şekilde çıkış yapıldı mesajı
     * @throws TokenBlacklistedException Eğer token kara listede ise
     */
    public SuccessResponse<AuthResponse> logout(String token) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) throw new TokenBlacklistedException();
        tokenBlacklistService.addTokenToBlacklist(token, jwtUtil.getExpirationDate(token));

        return createSuccessResponse(LOGOUT_SUCCESS);
    }

    /**
     * Refresh token ile yeni access ve refresh token üretir.
     *
     * @param refreshToken Kullanıcının mevcut refresh token'ı
     * @return Yeni üretilmiş access ve refresh token'ları içeren AuthResponse
     */
    public SuccessResponse<AuthResponse> refresh(String refreshToken) {
        validateRefreshToken(refreshToken);
        var email = jwtUtil.extractUsername(refreshToken);
        var response = generateAuthTokens(email);

        return createSuccessResponse(response, REFRESH_SUCCESS);
    }

    /**
     * Email ve şifre ile kimlik doğrulaması yapar.
     *
     * @param email    Email adresi
     * @param password Kullanıcı şifresi
     */
    private void authenticateUser(String email, String password) {
        var authToken = new UsernamePasswordAuthenticationToken(email, password);
        authenticationManager.authenticate(authToken);
    }

    /**
     * Kullanıcı için access ve refresh token üretir.
     *
     * @param email Email adresi
     * @return AuthResponse içinde yeni token bilgileri
     */
    private AuthResponse generateAuthTokens(String email) {
        var authorities = getUserAuthorities(email);
        var token = jwtUtil.generateToken(email, authorities);
        var refreshToken = jwtUtil.generateRefreshToken(email);

        return new AuthResponse(token, refreshToken);
    }

    /**
     * Refresh token'ı doğrular ve token tipi geçerli olup olmadığını kontrol eder.
     *
     * @param refreshToken Kullanıcının gönderdiği refresh token
     * @throws TokenTypeMismatchException Eğer token tipi geçerli değilse
     */
    private void validateRefreshToken(String refreshToken) {
        jwtUtil.validateToken(refreshToken);
        boolean isInvalidTokenType = !REFRESH_TOKEN_TYPE.equals(jwtUtil.getTokenType(refreshToken));
        if (isInvalidTokenType) throw new TokenTypeMismatchException(refreshToken);
    }

    /**
     * Kullanıcının yetkilerini getirir.
     *
     * @param email Email adresi
     * @return Kullanıcının sahip olduğu yetki listesi
     */
    private Collection<? extends GrantedAuthority> getUserAuthorities(String email) {
        return customUserDetailsService.loadUserByUsername(email).getAuthorities();
    }
}
