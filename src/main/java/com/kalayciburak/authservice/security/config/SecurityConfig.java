package com.kalayciburak.authservice.security.config;

import com.kalayciburak.authservice.constant.PublicEndpoints;
import com.kalayciburak.authservice.security.filter.JwtAuthenticationFilter;
import com.kalayciburak.authservice.security.handler.CustomAccessDeniedHandler;
import com.kalayciburak.authservice.security.handler.JwtAuthenticationEntryPoint;
import com.kalayciburak.authservice.security.token.RsaKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final RsaKeyService rsaKeyService;
    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF koruması devre dışı bırakılıyor (stateless API)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless oturum yönetimi
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PublicEndpoints.ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(authenticationEntryPoint);
                    exception.accessDeniedHandler(accessDeniedHandler);
                })
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Anonim kullanıcı otomatik olarak devre dışı bırakılıyor
                .anonymous(AbstractHttpConfigurer::disable);

        // JWT doğrulama filtresini ekle (UsernamePasswordAuthenticationFilter öncesinde)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS yapılandırması.
     * <p>
     * Bu yapılandırma, frontend uygulamanın bu auth-service ile iletişim kurabilmesi için gerekli CORS ayarlarını içerir.
     * </p>
     *
     * @return {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        var allowedOrigins = List.of("http://localhost:3000", "http://127.0.0.1:3000");
        var allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(Duration.ofHours(1).getSeconds());

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    /**
     * AuthenticationManager bean tanımı.
     * <p>
     * Kullanıcı adı ve şifre ile kimlik doğrulaması yapmak için kullanılır.
     *
     * @param configuration AuthenticationConfiguration
     * @return {@link AuthenticationManager}
     * @throws Exception Eğer AuthenticationManager oluşturulamazsa fırlatılır.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Parola şifreleme algoritması tanımı.
     *
     * @return {@link PasswordEncoder} deafult olarak {@link BCryptPasswordEncoder} döner.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Parola sızdırılmışlık kontrolü için HaveIBeenPwned API'sini kullanan bir bean tanımı.
     *
     * @return {@link HaveIBeenPwnedRestApiPasswordChecker}
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    /**
     * RSA public key ile JWT token'larını doğrulayan JwtDecoder bean'i.
     * <p>
     * Bu bean, diğer microservislerin bu auth-service'in ürettiği JWT token'larını doğrulaması için gereklidir.
     *
     * @return RSA public key ile yapılandırılmış {@link JwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeyService.getPublicKey()).build();
    }
}
