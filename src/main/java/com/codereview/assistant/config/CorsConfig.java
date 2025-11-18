package com.codereview.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 설정
 *
 * 프론트엔드 애플리케이션에서 API에 접근할 수 있도록 허용
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        // 프로덕션에서는 실제 프론트엔드 도메인으로 변경 필요
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React 개발 서버
            "http://localhost:5173",  // Vite 개발 서버
            "http://localhost:8080",  // 같은 서버
            "https://codereview.example.com"  // 프로덕션 도메인
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-GitHub-Event",
            "X-Hub-Signature-256",
            "X-Gitlab-Event",
            "X-Gitlab-Token",
            "X-Event-Key"
        ));

        // 노출할 헤더
        configuration.setExposedHeaders(List.of(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        // 사전 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
