package com.kalayciburak.authservice.security.token;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TokenBlacklistService sınıfının işlevselliğini test eden sınıftır. Bu test sınıfı, token'ın kara listeye eklenmesi, süresi
 * dolmuş tokenlarda ekleme yapılmaması ve token'ın kara listede olup olmadığının kontrolünü sağlamaktadır.
 * <p>
 * Her test metodunda ilgili senaryonun doğru şekilde ele alındığı doğrulanmaktadır.
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    private final String token = "test.jwt.token";
    private final String blacklistKey = "BLACKLIST:" + token;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    /**
     * Testler başlamadan önce gerekli ayarlamalar yapılır.
     */
    @BeforeEach
    void setUp() {
    }

    /**
     * Token'ın süresi dolmamış olduğunda kara listeye eklenme işleminin başarılı olduğunu test eder. Bu test, RedisTemplate
     * üzerinden opsForValue() çağrısının yapıldığını ve set metodunun doğru parametrelerle çağrıldığını doğrular.
     */
    @Test
    @DisplayName("Token kara listeye ekleme testi - token süresi dolmamış")
    void addTokenToBlacklistValidExpirationTest() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Arrange
        var futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 saat sonrası

        // Act
        tokenBlacklistService.addTokenToBlacklist(token, futureDate);

        // Verify
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
                eq(blacklistKey),
                eq("blacklisted"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS));
    }

    /**
     * Token'ın süresi dolmuş olduğunda kara listeye eklenme işlemi yapılmaması gerektiğini test eder. Bu durumda,
     * RedisTemplate ile hiçbir etkileşim olmamalıdır.
     */
    @Test
    @DisplayName("Token kara listeye ekleme testi - token süresi dolmuş")
    void addTokenToBlacklistExpiredTest() {
        // Arrange
        var pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 saat öncesi

        // Act
        tokenBlacklistService.addTokenToBlacklist(token, pastDate);

        // Verify
        verifyNoInteractions(redisTemplate);
        verifyNoMoreInteractions(valueOperations);
    }

    /**
     * Token'ın kara listede bulunduğu durumda, isTokenBlacklisted() metodunun true döndüğünü test eder.
     */
    @Test
    @DisplayName("Token kara listede kontrol testi - token kara listede")
    void isTokenBlacklistedWhenInBlacklistTest() {
        // Arrange
        when(redisTemplate.hasKey(blacklistKey)).thenReturn(true);

        // Act
        var result = tokenBlacklistService.isTokenBlacklisted(token);

        // Assert
        assertTrue(result, "Token kara listede ise sonuç true olmalıdır.");

        // Verify
        verify(redisTemplate).hasKey(blacklistKey);
    }

    /**
     * Token'ın kara listede bulunmadığı durumda, isTokenBlacklisted() metodunun false döndüğünü test eder.
     */
    @Test
    @DisplayName("Token kara listede kontrol testi - token kara listede değil")
    void isTokenBlacklistedWhenNotInBlacklistTest() {
        // Arrange
        when(redisTemplate.hasKey(blacklistKey)).thenReturn(false);

        // Act
        var result = tokenBlacklistService.isTokenBlacklisted(token);

        // Assert
        assertFalse(result, "Token kara listede değilse sonuç false olmalıdır.");

        // Verify
        verify(redisTemplate).hasKey(blacklistKey);
    }
}
