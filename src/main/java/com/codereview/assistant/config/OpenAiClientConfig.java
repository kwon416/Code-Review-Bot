package com.codereview.assistant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

/**
 * Configuration for OpenAI API client with proper HTTP client and retry logic
 */
@Configuration
@Slf4j
public class OpenAiClientConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi() {
        log.info("Configuring OpenAI API client with custom HTTP client");

        // Create a custom HTTP request factory with proper timeout and buffer settings
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000); // 30 seconds
        requestFactory.setReadTimeout(120000); // 120 seconds
        // Disable output streaming to allow retries
        requestFactory.setBufferRequestBody(true);

        // Create RestClient.Builder with custom request factory
        RestClient.Builder restClientBuilder = RestClient.builder()
            .requestFactory(requestFactory);

        return new OpenAiApi(apiKey, restClientBuilder);
    }

    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        log.info("Configuring OpenAI Chat Client with custom retry logic");

        // Create custom retry template with limited retries
        RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(2) // Only retry once
            .fixedBackoff(1000) // 1 second between retries
            .retryOn(org.springframework.web.client.ResourceAccessException.class)
            .retryOn(org.springframework.web.client.HttpServerErrorException.class)
            // Don't retry on client errors (4xx) like authentication failures
            .notRetryOn(org.springframework.web.client.HttpClientErrorException.class)
            .build();

        return OpenAiChatClient.builder(openAiApi)
            .withRetryTemplate(retryTemplate)
            .build();
    }
}
