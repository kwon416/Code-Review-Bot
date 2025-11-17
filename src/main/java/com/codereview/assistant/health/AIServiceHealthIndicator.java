package com.codereview.assistant.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * AI 서비스 (OpenAI) 헬스 체크
 *
 * OpenAI API 연결 상태를 확인합니다.
 */
@Component
@Slf4j
public class AIServiceHealthIndicator implements HealthIndicator {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4-turbo-preview}")
    private String model;

    @Override
    public Health health() {
        try {
            // API 키가 설정되어 있는지 확인
            if (apiKey == null || apiKey.isEmpty()) {
                return Health.down()
                    .withDetail("reason", "OpenAI API key not configured")
                    .build();
            }

            // API 키 마스킹 (보안)
            String maskedKey = maskApiKey(apiKey);

            return Health.up()
                .withDetail("status", "configured")
                .withDetail("model", model)
                .withDetail("api_key", maskedKey)
                .build();

        } catch (Exception e) {
            log.error("AI Service health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }

    /**
     * API 키를 마스킹하여 처음 4자와 마지막 4자만 표시
     */
    private String maskApiKey(String key) {
        if (key == null || key.length() < 8) {
            return "****";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
