package com.codereview.assistant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

/**
 * OpenAI API Configuration and Validation
 */
@Configuration
@Slf4j
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.model:gpt-4-turbo-preview}")
    private String model;

    /**
     * Validates OpenAI configuration on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("Validating OpenAI configuration...");

        if (!StringUtils.hasText(apiKey) || apiKey.equals("${OPENAI_API_KEY}")) {
            log.error("=================================================");
            log.error("OpenAI API Key is not configured!");
            log.error("Please set OPENAI_API_KEY environment variable");
            log.error("Example: export OPENAI_API_KEY=sk-xxxxxxxxxxxx");
            log.error("=================================================");
            log.warn("Application will continue but AI code review features will not work");
        } else if (!apiKey.startsWith("sk-")) {
            log.warn("=================================================");
            log.warn("OpenAI API Key format appears invalid");
            log.warn("Valid keys should start with 'sk-'");
            log.warn("Current key starts with: {}", apiKey.substring(0, Math.min(10, apiKey.length())));
            log.warn("=================================================");
        } else {
            log.info("OpenAI API Key is configured (key length: {})", apiKey.length());
            log.info("Using model: {}", model);
        }
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey)
            && !apiKey.equals("${OPENAI_API_KEY}")
            && apiKey.startsWith("sk-");
    }

    public boolean isApiKeyFormatValid() {
        return StringUtils.hasText(apiKey)
            && !apiKey.equals("${OPENAI_API_KEY}")
            && apiKey.startsWith("sk-");
    }

    public boolean hasApiKey() {
        return StringUtils.hasText(apiKey) && !apiKey.equals("${OPENAI_API_KEY}");
    }

    public String getModel() {
        return model;
    }
}
