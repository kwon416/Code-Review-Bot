package com.codereview.assistant.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Actuator 설정
 *
 * 헬스 체크 및 메트릭스 엔드포인트 설정
 */
@Configuration
public class ActuatorConfig {

    /**
     * 커스텀 헬스 인디케이터 - 애플리케이션 상태
     */
    @Bean
    public HealthIndicator applicationHealthIndicator() {
        return () -> Health.up()
            .withDetail("application", "CodeReview AI Assistant")
            .withDetail("version", "1.0.0")
            .withDetail("status", "running")
            .build();
    }
}
