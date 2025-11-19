package com.codereview.assistant.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Result of OpenAI API health check
 */
@Data
@Builder
public class OpenAiHealthCheckResult {

    /**
     * Overall health status
     */
    private boolean healthy;

    /**
     * Whether API key is configured
     */
    private boolean apiKeyConfigured;

    /**
     * Whether API key format is valid (starts with sk-)
     */
    private boolean apiKeyFormatValid;

    /**
     * Whether test mode is enabled
     */
    private boolean testModeEnabled;

    /**
     * Whether API connection test succeeded
     */
    private Boolean connectionSuccessful;

    /**
     * Response time in milliseconds (if connection was attempted)
     */
    private Long responseTimeMs;

    /**
     * Model being used
     */
    private String model;

    /**
     * Tokens used in test request (if connection was attempted)
     */
    private Integer tokensUsed;

    /**
     * Error message if any
     */
    private String errorMessage;

    /**
     * Detailed status message
     */
    private String statusMessage;
}
