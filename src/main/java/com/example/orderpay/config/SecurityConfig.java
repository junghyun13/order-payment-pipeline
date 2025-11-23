package com.example.orderpay.config;

import com.example.orderpay.auth.JwtFilter;
import com.example.orderpay.auth.JwtUtil;
import com.example.orderpay.auth.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    public SecurityConfig(JwtUtil jwtUtil, TokenBlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, blacklistService);

        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 최신 방식 CORS 적용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/signup","/auth/send-code", "/auth/verify-email","/auth/reset-password", "/auth/send-code-find", "/api/v1/orders/**","/index.html","/app.js",
                                "/",
                                "/favicon.ico",
                                "/js/**",
                                "/css/**",
                                "/images/**").permitAll()
                        .requestMatchers("/auth/logout").authenticated()            // 로그아웃은 JWT 필요
                        .anyRequest().authenticated()                                // 그 외 API도 JWT 필요
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

