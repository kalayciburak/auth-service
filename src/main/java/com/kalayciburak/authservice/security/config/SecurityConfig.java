package com.kalayciburak.authservice.security.config;

import com.kalayciburak.authservice.constant.PublicEndpoints;
import com.kalayciburak.authservice.security.filter.JwtAuthenticationFilter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF koruması devre dışı bırakılıyor (stateless API)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless oturum yönetimi
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Belirli endpoint'lere izin tanımlaması
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PublicEndpoints.ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        // JWT doğrulama filtresini ekle (UsernamePasswordAuthenticationFilter öncesinde)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
}
