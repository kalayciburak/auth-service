package com.kalayciburak.authservice.service;

import com.kalayciburak.authservice.advice.exception.TokenBlacklistedException;
import com.kalayciburak.authservice.advice.exception.TokenTypeMismatchException;
import com.kalayciburak.authservice.model.dto.request.LoginRequest;
import com.kalayciburak.authservice.model.entity.User;
import com.kalayciburak.authservice.repository.UserRepository;
import com.kalayciburak.authservice.security.token.JwtUtil;
import com.kalayciburak.authservice.security.token.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.kalayciburak.authservice.constant.JwtConstants.REFRESH_TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService sınıfının işlevselliğini test eden sınıftır. Bu test sınıfı, giriş, çıkış, token yenileme ve token tip
 * uyuşmazlığı gibi senaryoları kapsamlı şekilde kontrol ederek, ilgili iş mantığının beklendiği gibi çalıştığını doğrular.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private final String email = "test@test.com";
    private final String password = "password";
    private final String accessToken = "access.token.123";
    private final String refreshToken = "refresh.token.456";
    private final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_FREE"));

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;
    private User user;

    /**
     * Test öncesi gerekli ayarlamalar yapılır.
     */
    @BeforeEach
    void setUp() {
        userDetails = new org.springframework.security.core.userdetails.User(email, password, authorities);
        user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .emailVerified(true)
                .build();
    }

    /**
     * Giriş işleminin başarılı gerçekleştiğini test eder. Doğru kullanıcı adı ve şifre girildiğinde, access ve refresh
     * token'ların üretilip, ilgili servislere yönlendirme yapıldığını doğrular.
     */
    @Test
    @DisplayName("Başarılı giriş testi")
    void loginSuccessTest() {
        // Arrange: Login isteği oluşturulur.
        var request = new LoginRequest(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.generateToken(eq(email), anyCollection())).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(email)).thenReturn(refreshToken);

        // Act: Giriş işlemi gerçekleştirilir.
        var response = authService.login(request);

        // Assert: Üretilen token'lar ve yanıt nesnesi kontrol edilir.
        assertNotNull(response, "Giriş yanıtı null olmamalıdır.");
        assertNotNull(response.getData(), "AuthResponse verisi null olmamalıdır.");
        assertEquals(accessToken, response.getData().token(), "Access token beklenen değerle eşleşmelidir.");
        assertEquals(refreshToken, response.getData().refreshToken(), "Refresh token beklenen değerle eşleşmelidir.");
        assertTrue(response.isSuccess(), "Giriş işlemi başarılı olmalıdır.");

        // Verify: İlgili metod çağrıları doğrulanır.
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(customUserDetailsService).loadUserByUsername(email);
        verify(jwtUtil).generateToken(eq(email), anyCollection());
        verify(jwtUtil).generateRefreshToken(email);
    }

    /**
     * Çıkış işleminin başarılı gerçekleştiğini test eder. Token süresi dolmamışsa, token kara listeye eklenip, başarılı
     * çıkış yanıtı döndürülür.
     */
    @Test
    @DisplayName("Başarılı çıkış testi")
    void logoutSuccessTest() {
        // Arrange: Token'ın son kullanma tarihi ve kara liste kontrolü yapılır.
        var expirationDate = new Date(System.currentTimeMillis() + 3600000);
        when(tokenBlacklistService.isTokenBlacklisted(accessToken)).thenReturn(false);
        when(jwtUtil.getExpirationDate(accessToken)).thenReturn(expirationDate);
        doNothing().when(tokenBlacklistService).addTokenToBlacklist(accessToken, expirationDate);

        // Act: Çıkış işlemi gerçekleştirilir.
        var response = authService.logout(accessToken);

        // Assert: Çıkış yanıtının null olmadığı kontrol edilir.
        assertNotNull(response, "Çıkış yanıtı null olmamalıdır.");
        assertTrue(response.isSuccess(), "Çıkış işlemi başarılı olmalıdır.");

        // Verify: Kara liste kontrolü ve token son kullanma tarihi işlemleri
        // doğrulanır.
        verify(tokenBlacklistService).isTokenBlacklisted(accessToken);
        verify(tokenBlacklistService).addTokenToBlacklist(accessToken, expirationDate);
        verify(jwtUtil).getExpirationDate(accessToken);
    }

    /**
     * Token kara listede iken çıkış yapmaya çalışıldığında TokenBlacklistedException fırlatıldığını test eder.
     */
    @Test
    @DisplayName("Token kara listede iken çıkış yapma testi")
    void logoutWithBlacklistedTokenTest() {
        // Arrange: Token'ın kara listede olduğu durumu simüle edilir.
        when(tokenBlacklistService.isTokenBlacklisted(accessToken)).thenReturn(true);

        // Act & Assert: Kara listedeki token ile çıkış yapılmaya çalışıldığında
        // exception fırlatılması beklenir.
        assertThrows(TokenBlacklistedException.class, () -> authService.logout(accessToken),
                "Kara listede olan token ile çıkış yapılmaya çalışıldığında TokenBlacklistedException fırlatılmalıdır.");

        // Verify: Sadece isTokenBlacklisted çağrısı yapılmış olmalıdır.
        verify(tokenBlacklistService).isTokenBlacklisted(accessToken);
        verifyNoMoreInteractions(tokenBlacklistService);
        verifyNoInteractions(jwtUtil);
    }

    /**
     * Geçerli bir refresh token ile token yenileme işleminin başarılı gerçekleştiğini test eder. Refresh token geçerliyse,
     * yeni access token ve refresh token üretilip döndürülür.
     */
    @Test
    @DisplayName("Geçerli refresh token ile token yenileme testi")
    void refreshTokenSuccessTest() {
        // Arrange: Yeni refresh token üretilmesi senaryosu.
        var newRefreshToken = "new.refresh.token.789";

        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        doNothing().when(jwtUtil).validateToken(refreshToken);
        when(jwtUtil.getTokenType(refreshToken)).thenReturn(REFRESH_TOKEN_TYPE);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(jwtUtil.generateToken(eq(email), anyCollection())).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(email)).thenReturn(newRefreshToken);

        // Act: Token yenileme işlemi gerçekleştirilir.
        var response = authService.refresh(refreshToken);

        // Assert: Yeni token'ların doğru şekilde döndürüldüğü kontrol edilir.
        assertNotNull(response, "Token yenileme yanıtı null olmamalıdır.");
        assertNotNull(response.getData(), "AuthResponse verisi null olmamalıdır.");
        assertEquals(accessToken, response.getData().token(), "Yeni access token beklenen değerle eşleşmelidir.");
        assertEquals(newRefreshToken, response.getData().refreshToken(),
                "Yeni refresh token beklenen değerle eşleşmelidir.");
        assertTrue(response.isSuccess(), "Token yenileme işlemi başarılı olmalıdır.");

        // Verify: İlgili metod çağrıları doğrulanır.
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getTokenType(refreshToken);
        verify(jwtUtil).extractUsername(refreshToken);
        verify(customUserDetailsService).loadUserByUsername(email);
        verify(jwtUtil).generateToken(eq(email), anyCollection());
        verify(jwtUtil).generateRefreshToken(email);
    }

    /**
     * Yanlış token tipi ile token yenileme işlemi yapılmaya çalışıldığında TokenTypeMismatchException fırlatıldığını test
     * eder. Bu durumda, refresh token yerine access token tipi dönerse hata oluşması beklenir.
     */
    @Test
    @DisplayName("Yanlış token tipi ile yenileme testi")
    void refreshWithIncorrectTokenTypeTest() {
        // Arrange: Yanlış token tipi simüle edilir.
        doNothing().when(jwtUtil).validateToken(refreshToken);
        when(jwtUtil.getTokenType(refreshToken)).thenReturn("access"); // Yanlış token tipi

        // Act & Assert: Yanlış token tipi ile token yenileme denendiğinde exception
        // fırlatılması beklenir.
        assertThrows(TokenTypeMismatchException.class, () -> authService.refresh(refreshToken),
                "Yanlış token tipi ile yenileme işlemi yapıldığında TokenTypeMismatchException fırlatılmalıdır.");

        // Verify: Sadece validateToken ve getTokenType metodlarının çağrıldığı kontrol
        // edilir.
        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).getTokenType(refreshToken);
        verifyNoMoreInteractions(jwtUtil);
        verifyNoInteractions(customUserDetailsService);
    }
}
