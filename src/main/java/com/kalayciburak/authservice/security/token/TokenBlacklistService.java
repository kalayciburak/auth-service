package com.kalayciburak.authservice.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Verilen token'ı kara listeye ekler.
     *
     * @param token          Kara listeye alınacak token
     * @param expirationTime Token'in geçerlilik bitiş zamanı
     */
    public void addTokenToBlacklist(String token, Date expirationTime) {
        long timeout = expirationTime.getTime() - System.currentTimeMillis();
        if (timeout > 0) redisTemplate.opsForValue().set(getBlacklistKey(token), "blacklisted", timeout, MILLISECONDS);
    }

    /**
     * Token'ın kara listede olup olmadığını kontrol eder.
     *
     * @param token Kontrol edilecek token
     * @return Token kara listede ise true, değilse false
     */
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(getBlacklistKey(token));
    }

    /**
     * Token için Redis'te saklanacak kara liste anahtarını oluşturur.
     *
     * @param token Kara listeye alınacak token
     * @return Redis anahtarı olarak kullanılacak kara liste anahtarı
     */
    private static String getBlacklistKey(String token) {
        return "BLACKLIST:" + token;
    }
}
