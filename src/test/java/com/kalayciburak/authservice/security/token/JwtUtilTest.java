package com.kalayciburak.authservice.security.token;

import com.kalayciburak.authservice.advice.exception.InvalidJwtException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import static com.kalayciburak.authservice.constant.JwtConstants.ACCESS_TOKEN_TYPE;
import static com.kalayciburak.authservice.constant.JwtConstants.REFRESH_TOKEN_TYPE;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * JwtUtil sınıfının işlevselliğini test eden sınıftır. Bu sınıfta, access ve refresh token üretimi, token içerisindeki
 * bilgilerin çıkarılması, token doğrulaması ve token son kullanma tarihinin kontrolü gibi durumlar detaylıca test
 * edilmektedir.
 * <p>
 * Her test metodu, JwtUtil metodlarının beklenen davranışı sergilediğini doğrulamayı amaçlar.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilTest {
    private final String username = "testuser";
    private final long jwtExpirationMs = 3600000; // 1 saat
    private final List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"));

    @Mock
    private RsaKeyService rsaKeyService;

    @InjectMocks
    private JwtUtil jwtUtil;

    /**
     * Testler başlamadan önce gerekli nesneler oluşturulur.
     */
    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // RSA KeyPair oluşturma
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        var keyPair = keyPairGenerator.generateKeyPair();

        // Mock davranışları
        when(rsaKeyService.getPrivateKey()).thenReturn((RSAPrivateKey) keyPair.getPrivate());
        when(rsaKeyService.getPublicKey()).thenReturn((RSAPublicKey) keyPair.getPublic());
        when(rsaKeyService.getKeyId()).thenReturn("test-key-id");

        // @Value alanlarını test için ayarlama
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", jwtExpirationMs);
        // 1 gün
        long refreshExpirationMs = 86400000;
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationDateInMs", refreshExpirationMs);
    }

    /**
     * Access token oluşturma işleminin doğruluğunu test eder. Oluşturulan token'ın boş olmadığını, doğru username
     * içerdiğini, token tipinin ACCESS_TOKEN_TYPE olduğunu ve yetkilerin doğru şekilde eklendiğini doğrular.
     */
    @Test
    @DisplayName("Access token oluşturma testi")
    void generateTokenTest() {
        // Act
        var token = jwtUtil.generateToken(username, authorities);

        // Assert
        assertNotNull(token, "Token null olmamalıdır.");
        assertEquals(username, jwtUtil.extractUsername(token), "Username token içinden doğru şekilde çıkarılmalıdır.");
        assertEquals(ACCESS_TOKEN_TYPE, jwtUtil.getTokenType(token), "Token tipi 'access' olmalıdır.");

        var extractedAuthorities = jwtUtil.getAuthorities(token);
        assertEquals(2, extractedAuthorities.size(), "Token içinde iki yetki bulunmalıdır.");

        var hasUserRole = extractedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        var hasAdminRole = extractedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        assertTrue(hasUserRole, "Token içerisinde ROLE_USER yetkisi bulunmalıdır.");
        assertTrue(hasAdminRole, "Token içerisinde ROLE_ADMIN yetkisi bulunmalıdır.");
    }

    /**
     * Refresh token oluşturma işleminin doğruluğunu test eder. Oluşturulan token'ın boş olmadığını, doğru username
     * içerdiğini ve token tipinin REFRESH_TOKEN_TYPE olduğunu kontrol eder.
     */
    @Test
    @DisplayName("Refresh token oluşturma testi")
    void generateRefreshTokenTest() {
        // Act
        var token = jwtUtil.generateRefreshToken(username);

        // Assert
        assertNotNull(token, "Refresh token null olmamalıdır.");
        assertEquals(username, jwtUtil.extractUsername(token), "Username token içinden doğru şekilde çıkarılmalıdır.");
        assertEquals(REFRESH_TOKEN_TYPE, jwtUtil.getTokenType(token), "Token tipi 'refresh' olmalıdır.");
    }

    /**
     * Geçerli bir token kullanılarak token doğrulama işleminin başarılı olduğunu test eder.
     */
    @Test
    @DisplayName("Token doğrulama testi - Geçerli token")
    void validateTokenSuccessTest() {
        // Arrange
        var token = jwtUtil.generateToken(username, authorities);

        // Act & Assert
        assertDoesNotThrow(() -> jwtUtil.validateToken(token), "Geçerli token doğrulama sırasında hata oluşmamalıdır.");
    }

    /**
     * Yanlış formatta bir token verildiğinde InvalidJwtException fırlatıldığını test eder.
     */
    @Test
    @DisplayName("Token doğrulama testi - Geçersiz token formatı")
    void validateInvalidTokenFormatTest() {
        // Arrange
        var invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(InvalidJwtException.class, () -> jwtUtil.validateToken(invalidToken),
                "Geçersiz formatta token doğrulamada hata fırlatmalıdır.");
    }

    /**
     * Süresi dolmuş token durumunu test eder. Çok kısa ömürlü token oluşturup, token süresi dolduktan sonra doğrulama
     * yapılmaya çalışıldığında InvalidJwtException fırlatıldığını kontrol eder.
     */
    @Test
    @DisplayName("Token doğrulama testi - Süresi dolmuş token")
    void validateExpiredTokenTest() {
        // Arrange - 1 milisaniyelik ömür ile token oluşturulur.
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", 1L);
        var token = jwtUtil.generateToken(username, authorities);

        // Tokenin süresinin dolması için beklenir.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertThrows(InvalidJwtException.class, () -> jwtUtil.validateToken(token),
                "Süresi dolmuş token doğrulamada hata fırlatmalıdır.");
    }

    /**
     * Token içerisinden kullanıcı adının doğru şekilde çıkarıldığını test eder.
     */
    @Test
    @DisplayName("Kullanıcı adı çıkarma testi")
    void extractUsernameTest() {
        // Arrange
        var token = jwtUtil.generateToken(username, authorities);

        // Act
        var extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername, "Token içerisindeki username, orijinal username ile aynı olmalıdır.");
    }

    /**
     * Access token tipi belirleme işleminin doğruluğunu test eder.
     */
    @Test
    @DisplayName("Token tipini alma testi - Access token")
    void getTokenTypeAccessTest() {
        // Arrange
        var token = jwtUtil.generateToken(username, authorities);

        // Act
        var tokenType = jwtUtil.getTokenType(token);

        // Assert
        assertEquals(ACCESS_TOKEN_TYPE, tokenType, "Token tipi ACCESS_TOKEN_TYPE ile eşleşmelidir.");
    }

    /**
     * Refresh token tipi belirleme işleminin doğruluğunu test eder.
     */
    @Test
    @DisplayName("Token tipini alma testi - Refresh token")
    void getTokenTypeRefreshTest() {
        // Arrange
        var token = jwtUtil.generateRefreshToken(username);

        // Act
        var tokenType = jwtUtil.getTokenType(token);

        // Assert
        assertEquals(REFRESH_TOKEN_TYPE, tokenType, "Token tipi REFRESH_TOKEN_TYPE ile eşleşmelidir.");
    }

    /**
     * Token içerisindeki yetkilerin doğru şekilde çıkarıldığını test eder.
     */
    @Test
    @DisplayName("Token yetkileri alma testi")
    void getAuthoritiesTest() {
        // Arrange
        var token = jwtUtil.generateToken(username, authorities);

        // Act
        var extractedAuthorities = jwtUtil.getAuthorities(token);

        // Assert
        assertEquals(2, extractedAuthorities.size(), "Token içerisinde iki yetki bulunmalıdır.");

        var hasUserRole = extractedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        var hasAdminRole = extractedAuthorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        assertTrue(hasUserRole, "Token içerisinde ROLE_USER yetkisi bulunmalıdır.");
        assertTrue(hasAdminRole, "Token içerisinde ROLE_ADMIN yetkisi bulunmalıdır.");
    }

    /**
     * Token'in son kullanma tarihinin doğru şekilde belirlendiğini test eder. Token oluşturulduktan sonra, token'in son
     * kullanma tarihinin şu anki zamandan büyük olduğu ve beklenen sürede (jwtExpirationMs) sona erdiği kontrol edilir.
     */
    @Test
    @DisplayName("Token son kullanma tarihi alma testi")
    void getExpirationDateTest() {
        // Arrange
        var token = jwtUtil.generateToken(username, authorities);
        var now = System.currentTimeMillis();

        // Act
        var expirationDate = jwtUtil.getExpirationDate(token);

        // Assert
        assertNotNull(expirationDate, "Token son kullanma tarihi null olmamalıdır.");
        assertTrue(expirationDate.getTime() > now, "Token son kullanma tarihi, şu anki zamandan sonra olmalıdır.");
        var expectedExpiration = now + jwtExpirationMs;
        var isExpirationDateWithinTolerance = abs(expirationDate.getTime() - expectedExpiration) < 10000;
        assertTrue(isExpirationDateWithinTolerance, "Token son kullanma tarihi beklenen değere yakın olmalıdır.");
    }
}
