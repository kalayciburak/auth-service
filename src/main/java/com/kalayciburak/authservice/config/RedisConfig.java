package com.kalayciburak.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis bağlantı yapılandırmasını sağlayan konfigürasyon sınıfı.
 * <p>
 * Bu sınıf, Redis sunucusuna bağlantı sağlamak için {@link LettuceConnectionFactory} kullanır ve RedisTemplate üzerinden
 * veri işlemlerini yönetir.
 */
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * Redis bağlantısını oluşturan Bean.
     * <p>
     * <ol>
     *     <li>Redis sunucu adresi ve portu yapılandırılır.</li>
     *     <li>Şifre belirtilmişse yapılandırılır.</li>
     *     <li>{@link LettuceConnectionFactory} kullanılarak bağlantı oluşturulur.</li>
     * </ol>
     *
     * @return Redis bağlantısı için {@link LettuceConnectionFactory} Bean'i
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        var config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) config.setPassword(redisPassword);

        return new LettuceConnectionFactory(config);
    }

    /**
     * RedisTemplate Bean'i oluşturur ve gerekli serileştiricileri ayarlar.
     * <p>
     * <ol>
     *     <li>Bağlantı için {@link LettuceConnectionFactory} kullanılır.</li>
     *     <li>Anahtarlar için {@link StringRedisSerializer} kullanılır.</li>
     *     <li>Veri değerleri için {@link GenericJackson2JsonRedisSerializer} kullanılarak JSON formatında saklanır.</li>
     * </ol>
     *
     * @return Redis işlemleri için yapılandırılmış {@link RedisTemplate}
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
