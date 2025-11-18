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
    private final LanguageSpecificPromptService languageSpecificPromptService;

    // Use cheaper and faster model
    private static final String AI_MODEL = "gpt-4o-mini";
    private static final int MAX_DIFF_LENGTH = 6000; // Limit diff size to reduce tokens
    private static final int MAX_RESPONSE_TOKENS = 2000; // Limit response tokens

    /**
     * Analyzes code changes and returns review comments
     */
    public CodeReviewResult analyzeCode(String diffContent, String language) {
        log.info("Starting code analysis for language: {}", language);

        try {
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

            ChatResponse response = chatClient.call(new Prompt(prompt, options));

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis completed. Model: {}, Tokens used: {}", AI_MODEL, tokensUsed);

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

            ChatResponse response = chatClient.call(new Prompt(fullPrompt, options));

            String content = response.getResult().getOutput().getContent();
            int tokensUsed = response.getMetadata().getUsage().getTotalTokens().intValue();

            log.info("AI analysis with custom rules completed. Model: {}, Tokens used: {}", AI_MODEL, tokensUsed);

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

    /**
     * Truncates diff to reduce token usage
     * Prioritizes showing changes (+ and - lines) over context
     */
    private String truncateDiff(String diffContent) {
        if (diffContent.length() <= MAX_DIFF_LENGTH) {
            return diffContent;
        }

        // Extract only the important parts: changed files and actual changes
        String[] lines = diffContent.split("\n");
        StringBuilder truncated = new StringBuilder();
        int charCount = 0;
        boolean inDiff = false;

        for (String line : lines) {
            // Always include file headers
            if (line.startsWith("diff --git") || line.startsWith("+++") || line.startsWith("---")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;
                inDiff = true;
                continue;
            }

            // Include changed lines (+ or -)
            if (line.startsWith("+") || line.startsWith("-")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;

                if (charCount >= MAX_DIFF_LENGTH) {
                    truncated.append("\n... (diff truncated to reduce token usage) ...\n");
                    break;
                }
                continue;
            }

            // Include @@ chunk headers
            if (line.startsWith("@@")) {
                truncated.append(line).append("\n");
                charCount += line.length() + 1;
            }
        }

        return truncated.toString();
    }

    private String buildCodeReviewPrompt(String diffContent, String language) {
        return languageSpecificPromptService.buildCodeReviewPrompt(diffContent, language);
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
