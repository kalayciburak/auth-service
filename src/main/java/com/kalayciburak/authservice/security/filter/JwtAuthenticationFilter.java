package com.kalayciburak.authservice.security.filter;

import com.kalayciburak.authservice.advice.exception.InvalidJwtException;
import com.kalayciburak.authservice.advice.exception.TokenTypeMismatchException;
import com.kalayciburak.authservice.service.CustomUserDetailsService;
import com.kalayciburak.authservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.kalayciburak.authservice.constant.JwtConstants.ACCESS_TOKEN_TYPE;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var token = getBearerTokenFromRequest(request);
        if (token != null) authenticateRequestWithToken(token, request);
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP isteğinin "Authorization" başlığını kontrol eder ve "Bearer " ile başlayan token'ı ayrıştırır.
     *
     * @param request HTTP isteği
     * @return Bearer token veya null (eğer geçerli bir token yoksa)
     */
    private String getBearerTokenFromRequest(HttpServletRequest request) {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);
        var isValidBearerToken = StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX);

        return isValidBearerToken ? authHeader.substring(BEARER_PREFIX.length()) : null;
    }

    /**
     * Verilen JWT token ile isteği doğrular ve kullanıcıyı SecurityContext'e ekler.
     *
     * @param token   JWT token
     * @param request HTTP isteği
     */
    private void authenticateRequestWithToken(String token, HttpServletRequest request) {
        validateAccessToken(token);
        var username = jwtUtil.extractUsername(token);
        var userDetails = customUserDetailsService.loadUserByUsername(username);
        var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Verilen JWT token'in geçerli ve bir access token olup olmadığını kontrol eder.
     * <p>
     * <ul>
     *     <li>Eğer token geçersizse {@link InvalidJwtException} fırlatır.</li>
     *     <li>Eğer token bir access token değilse {@link TokenTypeMismatchException} fırlatır.</li>
     * </ul>
     *
     * @param token JWT token
     */
    private void validateAccessToken(String token) {
        jwtUtil.validateToken(token);
        var tokenType = jwtUtil.getTokenType(token);
        if (!ACCESS_TOKEN_TYPE.equals(tokenType)) throw new TokenTypeMismatchException(tokenType);
    }
}
