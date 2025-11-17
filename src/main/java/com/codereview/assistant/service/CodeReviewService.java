package com.codereview.assistant.service;

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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ReviewRuleService reviewRuleService;

    /**
     * Analyzes code changes and returns review comments
     */
    public CodeReviewResult analyzeCode(String diffContent, String language) {
        log.info("Starting code analysis for language: {}", language);

        try {
            String prompt = buildCodeReviewPrompt(diffContent, language);

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo-preview")
                .withTemperature(0.3)
                .build();

            ChatResponse response = chatClient.call(new Prompt(prompt, options));

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis completed. Tokens used: {}", tokensUsed);

            return parseCodeReviewResponse(content, tokensUsed);

        } catch (Exception e) {
            log.error("Error during code analysis", e);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary("Error occurred during code analysis: " + e.getMessage())
                .tokensUsed(0)
                .build();
        }
    }

    /**
     * Analyzes code with custom review rules
     */
    public CodeReviewResult analyzeCodeWithRules(String diffContent, String language, List<ReviewRule> customRules) {
        log.info("Starting code analysis with {} custom rules", customRules.size());

        try {
            // Build prompt with custom rules
            String basePrompt = buildCodeReviewPrompt(diffContent, language);
            String customPrompt = reviewRuleService.buildCustomPromptFromRules(customRules);
            String fullPrompt = basePrompt + customPrompt;

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-4-turbo-preview")
                .withTemperature(0.3)
                .build();

            ChatResponse response = chatClient.call(new Prompt(fullPrompt, options));

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis with custom rules completed. Tokens used: {}", tokensUsed);

            return parseCodeReviewResponse(content, tokensUsed);

        } catch (Exception e) {
            log.error("Error during code analysis with custom rules", e);
            return CodeReviewResult.builder()
                .comments(new ArrayList<>())
                .summary("Error occurred during code analysis: " + e.getMessage())
                .tokensUsed(0)
                .build();
        }
    }

    private String buildCodeReviewPrompt(String diffContent, String language) {
        return """
            You are an expert code reviewer. Analyze the following code diff and provide detailed feedback.

            Focus on:
            1. Bugs and potential errors
            2. Performance issues
            3. Security vulnerabilities
            4. Code style and best practices
            5. Maintainability concerns

            Language: %s

            Code Diff:
            ```
            %s
            ```

            Provide your review in the following JSON format:
            {
              "summary": "Overall summary of the code review",
              "comments": [
                {
                  "filePath": "path/to/file",
                  "lineNumber": 10,
                  "severity": "warning",
                  "category": "performance",
                  "message": "Brief description of the issue",
                  "suggestion": "How to fix or improve",
                  "codeExample": "Example of improved code (optional)"
                }
              ]
            }

            Severity levels: info, warning, error
            Categories: bug, performance, security, style, best-practice

            Only include meaningful comments. Skip trivial issues.
            """.formatted(language, diffContent);
    }

    private CodeReviewResult parseCodeReviewResponse(String response, int tokensUsed)
            throws JsonProcessingException {
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

        JsonNode rootNode = objectMapper.readTree(jsonContent);

        List<CodeReviewResult.ReviewComment> comments = new ArrayList<>();
        JsonNode commentsNode = rootNode.get("comments");
        if (commentsNode != null && commentsNode.isArray()) {
            for (JsonNode commentNode : commentsNode) {
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
            }
        }

        return CodeReviewResult.builder()
            .comments(comments)
            .summary(rootNode.has("summary") ? rootNode.get("summary").asText() : "")
            .tokensUsed(tokensUsed)
            .build();
    }
}
