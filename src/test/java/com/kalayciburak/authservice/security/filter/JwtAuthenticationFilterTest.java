package com.kalayciburak.authservice.security.filter;

import com.kalayciburak.authservice.security.token.JwtUtil;
import com.kalayciburak.authservice.security.token.TokenBlacklistService;
import com.kalayciburak.authservice.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import static com.kalayciburak.authservice.constant.JwtConstants.ACCESS_TOKEN_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void blacklistedTokenRejectsRequestWithoutContinuingFilterChain() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer blacklisted-token");
        when(jwtUtil.getTokenType("blacklisted-token")).thenReturn(ACCESS_TOKEN_TYPE);
        when(tokenBlacklistService.isTokenBlacklisted("blacklisted-token")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("Token kara listede.");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(filterChain, customUserDetailsService);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void validTokenAuthenticatesAndContinuesFilterChain() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var user = User.withUsername("test@example.com").password("unused").roles("FREE").build();
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtil.getTokenType("valid-token")).thenReturn(ACCESS_TOKEN_TYPE);
        when(jwtUtil.extractUsername("valid-token")).thenReturn(user.getUsername());
        when(customUserDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void requestWithoutTokenContinuesFilterChain() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, tokenBlacklistService, customUserDetailsService);
    }
}
