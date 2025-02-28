package com.kalayciburak.authservice.util;

import com.kalayciburak.authservice.advice.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

import static com.kalayciburak.authservice.constant.JwtConstants.*;
import static java.lang.System.currentTimeMillis;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long jwtExpirationInMs;
    private final long refreshExpirationDateInMs;

    /**
     * JwtUtil sınıfı için gerekli bağımlılıkları enjekte eder.
     *
     * @param secret                    Secret key
     * @param jwtExpirationInMs         JWT token'in geçerlilik süresi
     * @param refreshExpirationDateInMs Refresh token'in geçerlilik süresi
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long jwtExpirationInMs,
                   @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationDateInMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
        this.refreshExpirationDateInMs = refreshExpirationDateInMs;
    }

    /**
     * Kullanıcının yetkilerine göre access token oluşturur.
     *
     * @param username    Kullanıcı adı
     * @param authorities Kullanıcının yetkileri
     * @return Oluşturulan access token
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES_CLAIM, authorities.stream().map(GrantedAuthority::getAuthority).toList());
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

        return buildToken(username, claims, jwtExpirationInMs);
    }

    /**
     * Refresh token oluşturur.
     *
     * @param username Kullanıcı adı
     * @return Oluşturulan refresh token
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

        return buildToken(username, claims, refreshExpirationDateInMs);
    }

    /**
     * Token'in geçerliliğini kontrol eder.
     *
     * @param token JWT token
     * @throws InvalidJwtException Geçersiz veya süresi dolmuş token durumunda fırlatılır.
     */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtException(ex);
        }
    }

    /**
     * Token içerisinden kullanıcı adını çıkarır.
     *
     * @param token JWT token
     * @return Kullanıcı adı
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Token içerisinden "tokenType" claim'ini döner.
     *
     * @param token JWT token
     * @return Token tipi (access veya refresh)
     */
    public String getTokenType(String token) {
        return getClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * Token içerisindeki "roles" claim'ini okuyarak GrantedAuthority koleksiyonuna dönüştürür.
     *
     * @param token JWT token
     * @return Kullanıcının yetkileri
     */
    public Collection<SimpleGrantedAuthority> getAuthorities(String token) {
        return extractRoles(token).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Token oluşturma işlemlerindeki ortak adımlar burada toplanmıştır.
     *
     * @param username           Kullanıcı adı
     * @param claims             Ek claim'ler
     * @param expirationTimeInMs Token'in geçerlilik süresi (ms cinsinden)
     * @return Oluşturulan token
     */
    private String buildToken(String username, Map<String, Object> claims, long expirationTimeInMs) {
        var issuedAt = new Date();
        var expiration = new Date(currentTimeMillis() + expirationTimeInMs);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Token içerisinden "roles" claim'ini ayıklar ve String listesi olarak döner.
     *
     * @param token JWT token
     * @return Roller listesi
     */
    private List<String> extractRoles(String token) {
        List<?> rolesClaim = getClaims(token).get(ROLES_CLAIM, List.class);
        if (rolesClaim == null) return List.of();

        return rolesClaim.stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Token içerisindeki tüm claim'leri döner.
     *
     * @param token JWT token
     * @return Claim'lerin bulunduğu nesne
     */
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
