package com.codereview.assistant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))  // H2 Console iframe 허용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/webhook/**",
                    "/api/dashboard/**",
                    "/api/rules/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/h2-console/**",     // H2 Console 접근 허용
                    "/",                   // 홈 페이지
                    "/dashboard",          // 대시보드 페이지
                    "/css/**",             // CSS 파일
                    "/js/**",              // JavaScript 파일
                    "/favicon.ico"         // Favicon
                ).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
