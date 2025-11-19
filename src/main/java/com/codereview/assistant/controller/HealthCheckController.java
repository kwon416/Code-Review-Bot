package com.codereview.assistant.controller;

import com.codereview.assistant.dto.OpenAiHealthCheckResult;
import com.codereview.assistant.service.OpenAiHealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for health check endpoints
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health Check", description = "System health check APIs")
public class HealthCheckController {

    private final OpenAiHealthCheckService openAiHealthCheckService;

    /**
     * Check OpenAI API health and configuration
     *
     * @param testConnection whether to perform actual API connection test (costs tokens)
     * @return health check result
     */
    @GetMapping("/openai")
    @Operation(
        summary = "Check OpenAI API health",
        description = "Checks OpenAI API configuration and optionally tests connectivity. " +
                     "Note: Connection test will consume OpenAI API tokens."
    )
    public ResponseEntity<OpenAiHealthCheckResult> checkOpenAiHealth(
        @Parameter(description = "Perform actual API connection test (costs tokens)")
        @RequestParam(defaultValue = "false") boolean testConnection
    ) {
        log.info("OpenAI health check requested (testConnection: {})", testConnection);

        OpenAiHealthCheckResult result = openAiHealthCheckService.checkHealth(testConnection);

        if (result.isHealthy()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * Quick health check without API call
     */
    @GetMapping("/openai/quick")
    @Operation(
        summary = "Quick OpenAI API configuration check",
        description = "Checks OpenAI API configuration without making actual API calls. " +
                     "This endpoint does not consume any tokens."
    )
    public ResponseEntity<OpenAiHealthCheckResult> quickCheck() {
        log.info("OpenAI quick health check requested");
        OpenAiHealthCheckResult result = openAiHealthCheckService.checkHealth(false);

        if (result.isHealthy()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * Full health check with API connection test
     */
    @GetMapping("/openai/full")
    @Operation(
        summary = "Full OpenAI API health check with connection test",
        description = "Performs comprehensive health check including actual API connection test. " +
                     "WARNING: This will consume OpenAI API tokens."
    )
    public ResponseEntity<OpenAiHealthCheckResult> fullCheck() {
        log.info("OpenAI full health check requested");
        OpenAiHealthCheckResult result = openAiHealthCheckService.checkHealth(true);

        if (result.isHealthy()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(503).body(result);
        }
    }
}
