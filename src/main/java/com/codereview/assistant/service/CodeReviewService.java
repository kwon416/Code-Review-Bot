package com.codereview.assistant.service;

import com.codereview.assistant.dto.CodeReviewResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final LanguageSpecificPromptService languageSpecificPromptService;

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

            ChatResponse response = chatModel.call(new Prompt(prompt, options));

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
