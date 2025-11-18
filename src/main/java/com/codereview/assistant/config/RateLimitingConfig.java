package com.codereview.assistant.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting 설정
 *
 * API 남용을 방지하기 위한 요청 제한 설정
 * - Dashboard API: 분당 60개 요청
 * - Webhook API: 분당 100개 요청
 */
@Configuration
@Slf4j
public class RateLimitingConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

    static class RateLimitInterceptor implements HandlerInterceptor {
        private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String key = getClientKey(request);
            Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(request));

            if (bucket.tryConsume(1)) {
                return true;
            }

            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "error": "Too Many Requests",
                  "message": "Rate limit exceeded. Please try again later."
                }
                """);
            log.warn("Rate limit exceeded for client: {}", key);
            return false;
        }

        private String getClientKey(HttpServletRequest request) {
            String clientIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            return clientIp + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
        }

        private Bucket createBucket(HttpServletRequest request) {
            String path = request.getRequestURI();

            // Webhook endpoints: 100 requests per minute
            if (path.startsWith("/api/webhook/")) {
                Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
                return Bucket.builder()
                    .addLimit(limit)
                    .build();
            }

            // Dashboard and other APIs: 60 requests per minute
            Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
            return Bucket.builder()
                .addLimit(limit)
                .build();
        }
    }
}
