package com.codereview.assistant.service;

import com.codereview.assistant.config.OpenAiConfig;
import com.codereview.assistant.dto.OpenAiHealthCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for checking OpenAI API health and connectivity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiHealthCheckService {

    private final ChatClient chatClient;
    private final OpenAiConfig openAiConfig;

    @Value("${app.test-mode:true}")
    private boolean testMode;

    private static final String TEST_PROMPT = "Say 'OK' if you can read this.";
    private static final String AI_MODEL = "gpt-4o-mini";

    /**
     * Performs a comprehensive health check of OpenAI API
     *
     * @param performConnectionTest whether to actually call OpenAI API (costs tokens)
     * @return health check result
     */
    public OpenAiHealthCheckResult checkHealth(boolean performConnectionTest) {
        log.info("Starting OpenAI API health check (performConnectionTest: {})", performConnectionTest);

        OpenAiHealthCheckResult.OpenAiHealthCheckResultBuilder builder = OpenAiHealthCheckResult.builder();

        // Check API key configuration
        boolean apiKeyConfigured = openAiConfig.hasApiKey();
        boolean apiKeyFormatValid = openAiConfig.isApiKeyFormatValid();

        builder.apiKeyConfigured(apiKeyConfigured)
               .apiKeyFormatValid(apiKeyFormatValid)
               .testModeEnabled(testMode)
               .model(AI_MODEL);

        // If test mode is enabled
        if (testMode) {
            builder.healthy(true)
                   .connectionSuccessful(null)
                   .statusMessage("Test mode is enabled. OpenAI API calls are bypassed with mock responses.");
            log.info("Health check completed: Test mode enabled");
            return builder.build();
        }

        // If API key is not configured
        if (!apiKeyConfigured) {
            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage("OpenAI API key is not configured. Please set OPENAI_API_KEY environment variable.")
                   .statusMessage("API key not configured");
            log.warn("Health check completed: API key not configured");
            return builder.build();
        }

        // If API key format is invalid
        if (!apiKeyFormatValid) {
            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage("OpenAI API key format is invalid. Valid keys should start with 'sk-'.")
                   .statusMessage("API key format invalid");
            log.warn("Health check completed: API key format invalid");
            return builder.build();
        }

        // If connection test is not requested, return configuration check only
        if (!performConnectionTest) {
            builder.healthy(true)
                   .connectionSuccessful(null)
                   .statusMessage("API key is configured correctly. Connection test was not performed.");
            log.info("Health check completed: API key configured, connection test skipped");
            return builder.build();
        }

        // Perform actual API connection test
        try {
            log.info("Performing OpenAI API connection test...");
            long startTime = System.currentTimeMillis();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(AI_MODEL)
                .withTemperature(0.0f)
                .withMaxTokens(10)
                .build();

            ChatResponse response = chatClient.call(new Prompt(TEST_PROMPT, options));
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("OpenAI API connection test successful. Response time: {}ms, Tokens used: {}",
                     responseTime, tokensUsed);

            builder.healthy(true)
                   .connectionSuccessful(true)
                   .responseTimeMs(responseTime)
                   .tokensUsed(tokensUsed)
                   .statusMessage("OpenAI API is healthy and responding correctly. Response: " + content);

            return builder.build();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("OpenAI API connection test failed with client error ({}): {}",
                      e.getStatusCode(), e.getMessage());

            String errorMsg;
            if (e.getStatusCode().value() == 401) {
                errorMsg = "Authentication failed. API key is invalid or expired.";
            } else if (e.getStatusCode().value() == 429) {
                errorMsg = "Rate limit exceeded. Please try again later.";
            } else {
                errorMsg = "Client error: " + e.getMessage();
            }

            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage(errorMsg)
                   .statusMessage("OpenAI API connection test failed");

            return builder.build();

        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("OpenAI API connection test failed with server error ({}): {}",
                      e.getStatusCode(), e.getMessage());

            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage("OpenAI API server error: " + e.getMessage())
                   .statusMessage("OpenAI API is experiencing issues");

            return builder.build();

        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("OpenAI API connection test failed with network error: {}", e.getMessage());

            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage("Network error: " + e.getMessage())
                   .statusMessage("Cannot connect to OpenAI API");

            return builder.build();

        } catch (Exception e) {
            log.error("OpenAI API connection test failed with unexpected error", e);

            builder.healthy(false)
                   .connectionSuccessful(false)
                   .errorMessage("Unexpected error: " + e.getMessage())
                   .statusMessage("Health check failed");

            return builder.build();
        }
    }
}
