package com.codereview.assistant.service;

import com.codereview.assistant.config.OpenAiConfig;
import com.codereview.assistant.domain.ReviewRule;
import com.codereview.assistant.dto.CodeReviewResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ReviewRuleService reviewRuleService;
    private final LanguageSpecificPromptService languageSpecificPromptService;
    private final OpenAiConfig openAiConfig;

    // Use cheaper and faster model with aggressive token optimization
    private static final String AI_MODEL = "gpt-4o-mini";
    private static final int MAX_DIFF_LENGTH = 2500; // Aggressive token reduction
    private static final int MAX_RESPONSE_TOKENS = 800; // Minimal response tokens

    // Test mode: when true, returns fixed test response instead of calling GPT API
    @Value("${app.test-mode:true}")
    private boolean testMode;

    /**
     * Analyzes code changes and returns review comments
     */
    public CodeReviewResult analyzeCode(String diffContent, String language) {
        log.info("Starting code analysis for language: {}", language);

        // Validate OpenAI configuration first
        if (!openAiConfig.isConfigured()) {
            String errorMsg = "OpenAI API Key is not configured. Please set OPENAI_API_KEY environment variable.";
            log.error(errorMsg);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary(errorMsg)
                .tokensUsed(0)
                .build();
        }

        try {
            // TEST MODE: Return fixed response instead of calling GPT API
            if (testMode) {
                log.warn("⚠️ TEST MODE ENABLED - Using fixed test response instead of GPT API");
                String testResponse = getTestResponse();
                CodeReviewResult result = parseCodeReviewResponse(testResponse, 100);
                log.info("✅ TEST: Parsed {} comments from test response", result.getComments().size());
                return result;
            }

            // Truncate diff if too large to reduce token usage
            String processedDiff = truncateDiff(diffContent);
            int originalLength = diffContent.length();
            int processedLength = processedDiff.length();

            if (originalLength > processedLength) {
                log.warn("Diff truncated from {} to {} characters to reduce token usage",
                    originalLength, processedLength);
            }

            String prompt = buildCodeReviewPrompt(processedDiff, language);

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(AI_MODEL)
                .withTemperature(0.3f)
                .withMaxTokens(MAX_RESPONSE_TOKENS)
                .build();

            ChatResponse response;
            try {
                response = chatClient.call(new Prompt(prompt, options));
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Client error (4xx) - likely authentication or invalid request
                log.error("OpenAI API client error ({}): {}", e.getStatusCode(), e.getMessage());
                throw new RuntimeException("OpenAI API authentication or request error: " + e.getMessage(), e);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // Server error (5xx) - OpenAI API issue
                log.error("OpenAI API server error ({}): {}", e.getStatusCode(), e.getMessage());
                throw new RuntimeException("OpenAI API server error: " + e.getMessage(), e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Network error - timeout, connection refused, etc.
                log.error("Network error connecting to OpenAI API: {}", e.getMessage());
                throw new RuntimeException("Network error connecting to OpenAI API: " + e.getMessage(), e);
            }

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis completed. Model: {}, Tokens used: {}", AI_MODEL, tokensUsed);
            log.debug("AI response content: {}", content);

            CodeReviewResult result = parseCodeReviewResponse(content, tokensUsed);
            log.info("Parsed {} comments from AI response", result.getComments().size());

            return result;

        } catch (RestClientException e) {
            return handleRestClientException(e);
        } catch (Exception e) {
            log.error("Unexpected error during code analysis", e);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary("Unexpected error occurred during code analysis: " + e.getMessage())
                .tokensUsed(0)
                .build();
        }
    }

    /**
     * Analyzes code with custom review rules
     */
    public CodeReviewResult analyzeCodeWithRules(String diffContent, String language, List<ReviewRule> customRules) {
        log.info("Starting code analysis with {} custom rules", customRules.size());

        // Validate OpenAI configuration first
        if (!openAiConfig.isConfigured()) {
            String errorMsg = "OpenAI API Key is not configured. Please set OPENAI_API_KEY environment variable.";
            log.error(errorMsg);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary(errorMsg)
                .tokensUsed(0)
                .build();
        }

        try {
            // TEST MODE: Return fixed response instead of calling GPT API
            if (testMode) {
                log.warn("⚠️ TEST MODE ENABLED - Using fixed test response instead of GPT API");
                String testResponse = getTestResponse();
                CodeReviewResult result = parseCodeReviewResponse(testResponse, 100);
                log.info("✅ TEST: Parsed {} comments from test response", result.getComments().size());
                return result;
            }

            // Truncate diff if too large
            String processedDiff = truncateDiff(diffContent);

            // Build prompt with custom rules
            String basePrompt = buildCodeReviewPrompt(processedDiff, language);
            String customPrompt = reviewRuleService.buildCustomPromptFromRules(customRules);
            String fullPrompt = basePrompt + customPrompt;

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(AI_MODEL)
                .withTemperature(0.3f)
                .withMaxTokens(MAX_RESPONSE_TOKENS)
                .build();

            ChatResponse response;
            try {
                response = chatClient.call(new Prompt(fullPrompt, options));
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // Client error (4xx) - likely authentication or invalid request
                log.error("OpenAI API client error ({}): {}", e.getStatusCode(), e.getMessage());
                throw new RuntimeException("OpenAI API authentication or request error: " + e.getMessage(), e);
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // Server error (5xx) - OpenAI API issue
                log.error("OpenAI API server error ({}): {}", e.getStatusCode(), e.getMessage());
                throw new RuntimeException("OpenAI API server error: " + e.getMessage(), e);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Network error - timeout, connection refused, etc.
                log.error("Network error connecting to OpenAI API: {}", e.getMessage());
                throw new RuntimeException("Network error connecting to OpenAI API: " + e.getMessage(), e);
            }

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis with custom rules completed. Model: {}, Tokens used: {}", AI_MODEL, tokensUsed);
            log.debug("AI response content: {}", content);

            CodeReviewResult result = parseCodeReviewResponse(content, tokensUsed);
            log.info("Parsed {} comments from AI response", result.getComments().size());

            return result;

        } catch (RestClientException e) {
            return handleRestClientException(e);
        } catch (Exception e) {
            log.error("Unexpected error during code analysis with custom rules", e);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary("Unexpected error occurred during code analysis: " + e.getMessage())
                .tokensUsed(0)
                .build();
        }
    }

    /**
     * Truncates diff to reduce token usage significantly
     * Prioritizes important code changes and excludes noise
     */
    private String truncateDiff(String diffContent) {
        // Extract only the important parts: changed files and actual changes
        String[] lines = diffContent.split("\n");
        StringBuilder truncated = new StringBuilder();
        int charCount = 0;
        String currentFile = null;
        boolean skipCurrentFile = false;

        for (String line : lines) {
            // Check for file headers
            if (line.startsWith("diff --git")) {
                currentFile = line;
                skipCurrentFile = shouldSkipFile(line);

                if (!skipCurrentFile) {
                    truncated.append(line).append("\n");
                    charCount += line.length() + 1;
                }
                continue;
            }

            // Skip if we're ignoring this file
            if (skipCurrentFile) {
                continue;
            }

            // Include file path headers (only for non-skipped files)
            if (line.startsWith("+++") || line.startsWith("---")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;
                continue;
            }

            // Include @@ chunk headers
            if (line.startsWith("@@")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;
                continue;
            }

            // Only include changed lines (+ or -)
            // Skip context lines (no prefix) to save tokens
            if (line.startsWith("+") || line.startsWith("-")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;

                if (charCount >= MAX_DIFF_LENGTH) {
                    truncated.append("\n... (truncated - ").append(lines.length).append(" total lines) ...\n");
                    break;
                }
            }
        }

        return truncated.toString();
    }

    /**
     * Determines if a file should be skipped in diff analysis
     * Excludes files that don't need code review (binary, lock files, etc.)
     */
    private boolean shouldSkipFile(String diffLine) {
        String lowerLine = diffLine.toLowerCase();

        // Skip lock files and dependency manifests
        if (lowerLine.contains("package-lock.json") ||
            lowerLine.contains("yarn.lock") ||
            lowerLine.contains("pnpm-lock.yaml") ||
            lowerLine.contains("composer.lock") ||
            lowerLine.contains("gemfile.lock") ||
            lowerLine.contains("poetry.lock") ||
            lowerLine.contains("cargo.lock") ||
            lowerLine.contains("go.sum") ||
            lowerLine.contains("go.mod")) {
            return true;
        }

        // Skip generated files and build outputs
        if (lowerLine.contains("/build/") ||
            lowerLine.contains("/dist/") ||
            lowerLine.contains("/target/") ||
            lowerLine.contains("/.gradle/") ||
            lowerLine.contains("/node_modules/") ||
            lowerLine.contains("/vendor/") ||
            lowerLine.contains("/__pycache__/") ||
            lowerLine.contains(".min.js") ||
            lowerLine.contains(".min.css") ||
            lowerLine.contains(".bundle.js") ||
            lowerLine.contains(".bundle.css")) {
            return true;
        }

        // Skip binary and media files
        if (lowerLine.contains(".png") ||
            lowerLine.contains(".jpg") ||
            lowerLine.contains(".jpeg") ||
            lowerLine.contains(".gif") ||
            lowerLine.contains(".svg") ||
            lowerLine.contains(".ico") ||
            lowerLine.contains(".webp") ||
            lowerLine.contains(".pdf") ||
            lowerLine.contains(".zip") ||
            lowerLine.contains(".tar") ||
            lowerLine.contains(".gz") ||
            lowerLine.contains(".jar") ||
            lowerLine.contains(".war") ||
            lowerLine.contains(".ear") ||
            lowerLine.contains(".exe") ||
            lowerLine.contains(".dll") ||
            lowerLine.contains(".so") ||
            lowerLine.contains(".dylib") ||
            lowerLine.contains(".a") ||
            lowerLine.contains(".o")) {
            return true;
        }

        // Skip test snapshots and fixtures
        if (lowerLine.contains("/__snapshots__/") ||
            lowerLine.contains("/fixtures/") ||
            lowerLine.contains("/test-data/") ||
            lowerLine.contains(".snap")) {
            return true;
        }

        // Skip configuration and metadata files
        if (lowerLine.endsWith(".lock") ||
            lowerLine.endsWith(".sum") ||
            lowerLine.endsWith(".cache") ||
            lowerLine.contains(".env.example") ||
            lowerLine.contains(".gitignore") ||
            lowerLine.contains(".dockerignore")) {
            return true;
        }

        // Skip documentation images and assets
        if ((lowerLine.contains("/docs/") || lowerLine.contains("/documentation/")) &&
            (lowerLine.contains(".png") || lowerLine.contains(".jpg") || lowerLine.contains(".gif"))) {
            return true;
        }

        return false;
    }

    private String buildCodeReviewPrompt(String diffContent, String language) {
        return languageSpecificPromptService.buildCodeReviewPrompt(diffContent, language);
    }

    /**
     * Handles RestClient exceptions and returns appropriate error messages
     */
    private CodeReviewResult handleRestClientException(RestClientException e) {
        String errorMessage;

        // Check for HttpRetryException in the cause chain (authentication error)
        if (containsCauseType(e, HttpRetryException.class)) {

            errorMessage = "OpenAI API Authentication Failed. " +
                          "This usually indicates an invalid or missing API key. " +
                          "Please verify your OPENAI_API_KEY environment variable is set correctly. " +
                          "Make sure the API key starts with 'sk-' and is valid.";
            log.error("HTTP Retry Exception (Authentication): {}", e.getMessage(), e);

        } else if (e.getMessage() != null &&
            (e.getMessage().contains("authentication") ||
             e.getMessage().contains("Unauthorized") ||
             e.getMessage().contains("401"))) {

            errorMessage = "OpenAI API Authentication Failed. " +
                          "Please verify your OPENAI_API_KEY environment variable is set correctly. " +
                          "Make sure the API key starts with 'sk-' and is valid.";
            log.error("Authentication error with OpenAI API: {}", e.getMessage());

        } else if (e.getMessage() != null &&
                   (e.getMessage().contains("429") || e.getMessage().contains("rate limit"))) {

            errorMessage = "OpenAI API rate limit exceeded. Please try again later.";
            log.error("Rate limit error: {}", e.getMessage());

        } else if (e.getMessage() != null &&
                   (e.getMessage().contains("quota") || e.getMessage().contains("insufficient_quota"))) {

            errorMessage = "OpenAI API quota exceeded. Please check your OpenAI account billing and usage.";
            log.error("Quota error: {}", e.getMessage());

        } else if (e instanceof ResourceAccessException) {

            errorMessage = "Failed to connect to OpenAI API. Please check your network connection.";
            log.error("Network error: {}", e.getMessage());

        } else {

            errorMessage = "OpenAI API error: " + e.getMessage();
            log.error("RestClient error during code analysis: {}", e.getMessage(), e);
        }

        return CodeReviewResult.builder()
            .comments(new ArrayList<>())
            .summary(errorMessage)
            .tokensUsed(0)
            .build();
    }

    /**
     * Checks if the exception's cause chain contains a specific exception type
     */
    private boolean containsCauseType(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private CodeReviewResult parseCodeReviewResponse(String response, int tokensUsed)
            throws JsonProcessingException {
        try {
            // Extract JSON from markdown code blocks if present
            String jsonContent = response;
            if (response.contains("```json")) {
                jsonContent = response.substring(
                    response.indexOf("```json") + 7,
                    response.lastIndexOf("```")
                ).trim();
            } else if (response.contains("```")) {
                jsonContent = response.substring(
                    response.indexOf("```") + 3,
                    response.lastIndexOf("```")
                ).trim();
            }

            log.debug("Extracted JSON content: {}", jsonContent);

            JsonNode rootNode = objectMapper.readTree(jsonContent);

            List<CodeReviewResult.ReviewComment> comments = new ArrayList<>();
            JsonNode commentsNode = rootNode.get("comments");

            if (commentsNode == null) {
                log.warn("No 'comments' field found in AI response");
            } else if (!commentsNode.isArray()) {
                log.warn("'comments' field is not an array in AI response");
            } else {
                log.debug("Found {} comments in AI response", commentsNode.size());

                for (JsonNode commentNode : commentsNode) {
                    try {
                        CodeReviewResult.ReviewComment comment = CodeReviewResult.ReviewComment.builder()
                            .filePath(commentNode.get("filePath").asText())
                            .lineNumber(commentNode.has("lineNumber") ?
                                commentNode.get("lineNumber").asInt() : null)
                            .severity(commentNode.get("severity").asText())
                            .category(commentNode.get("category").asText())
                            .message(commentNode.get("message").asText())
                            .suggestion(commentNode.has("suggestion") ?
                                commentNode.get("suggestion").asText() : null)
                            .codeExample(commentNode.has("codeExample") ?
                                commentNode.get("codeExample").asText() : null)
                            .build();
                        comments.add(comment);
                    } catch (Exception e) {
                        log.error("Failed to parse individual comment: {}", commentNode, e);
                    }
                }
            }

            String summary = rootNode.has("summary") ? rootNode.get("summary").asText() : "No summary provided";

            return CodeReviewResult.builder()
                .comments(comments)
                .summary(summary)
                .tokensUsed(tokensUsed)
                .build();
        } catch (Exception e) {
            log.error("Failed to parse AI response. Response: {}", response, e);
            throw e;
        }
    }

    /**
     * Returns a fixed test response for debugging without calling GPT API
     * Used when app.test-mode=true
     */
    private String getTestResponse() {
        return """
            {
              "summary": "🧪 테스트 모드: GitHub PR 코멘트 게시 기능이 정상적으로 작동하는지 확인하기 위한 고정 테스트 응답입니다.",
              "comments": [
                {
                  "filePath": "src/main/java/com/codereview/assistant/service/CodeReviewService.java",
                  "lineNumber": 35,
                  "severity": "warning",
                  "category": "test",
                  "message": "테스트 코멘트 #1: GitHub API 연동이 정상적으로 작동하는지 확인하는 테스트 코멘트입니다.",
                  "suggestion": "이것은 단순 테스트이므로 조치가 필요하지 않습니다.",
                  "codeExample": "// 테스트 코드 예시입니다"
                },
                {
                  "filePath": "src/main/java/com/codereview/assistant/service/ReviewService.java",
                  "lineNumber": 45,
                  "severity": "info",
                  "category": "test",
                  "message": "테스트 코멘트 #2: 여러 개의 코멘트를 성공적으로 게시할 수 있는지 확인합니다.",
                  "suggestion": "GitHub PR에서 이 코멘트가 보인다면 연동이 정상적으로 작동하는 것입니다!"
                },
                {
                  "filePath": "README.md",
                  "lineNumber": 1,
                  "severity": "error",
                  "category": "test",
                  "message": "테스트 코멘트 #3: Error 심각도 표시를 테스트합니다.",
                  "suggestion": "이 코멘트는 빨간색 에러 아이콘과 함께 표시되어야 합니다.",
                  "codeExample": "# 테스트 예시\\n이것은 테스트용입니다"
                }
              ]
            }
            """;
    }
}
