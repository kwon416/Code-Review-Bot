package com.codereview.assistant.service;

import com.codereview.assistant.dto.CodeReviewResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.AssistantMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeReviewService 테스트")
class CodeReviewServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ReviewRuleService reviewRuleService;

    @Mock
    private LanguageSpecificPromptService languageSpecificPromptService;

    @InjectMocks
    private CodeReviewService codeReviewService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Use reflection to inject objectMapper
        try {
            var field = CodeReviewService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(codeReviewService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("코드 분석 성공 - JSON 응답")
    void analyzeCode_Success() {
        // Given
        String diffContent = """
            diff --git a/Test.java b/Test.java
            +public class Test {
            +    private String password = "12345";
            +}
            """;
        String language = "Java";

        String aiResponse = """
            ```json
            {
              "comments": [
                {
                  "filePath": "Test.java",
                  "lineNumber": 2,
                  "severity": "error",
                  "category": "security",
                  "message": "하드코딩된 비밀번호 발견",
                  "suggestion": "환경 변수 또는 설정 파일 사용"
                }
              ],
              "summary": "보안 이슈 1건 발견"
            }
            ```
            """;

        when(languageSpecificPromptService.buildCodeReviewPrompt(diffContent, language))
            .thenReturn("Test prompt");

        ChatResponse chatResponse = mockChatResponse(aiResponse, 1000);
        when(chatClient.call(any())).thenReturn(chatResponse);

        // When
        CodeReviewResult result = codeReviewService.analyzeCode(diffContent, language);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getSeverity()).isEqualTo("error");
        assertThat(result.getComments().get(0).getCategory()).isEqualTo("security");
        assertThat(result.getTokensUsed()).isEqualTo(1000);
    }

    @Test
    @DisplayName("코드 분석 - 에러 처리")
    void analyzeCode_ErrorHandling() {
        // Given
        String diffContent = "test diff";
        String language = "Java";

        when(languageSpecificPromptService.buildCodeReviewPrompt(diffContent, language))
            .thenThrow(new RuntimeException("API Error"));

        // When
        CodeReviewResult result = codeReviewService.analyzeCode(diffContent, language);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getSummary()).contains("Error occurred");
        assertThat(result.getTokensUsed()).isZero();
    }

    @Test
    @DisplayName("코드 분석 - 빈 응답")
    void analyzeCode_EmptyResponse() {
        // Given
        String diffContent = "test diff";
        String language = "Python";

        String aiResponse = """
            {
              "comments": [],
              "summary": "이슈 없음"
            }
            """;

        when(languageSpecificPromptService.buildCodeReviewPrompt(diffContent, language))
            .thenReturn("Test prompt");

        ChatResponse chatResponse = mockChatResponse(aiResponse, 500);
        when(chatClient.call(any())).thenReturn(chatResponse);

        // When
        CodeReviewResult result = codeReviewService.analyzeCode(diffContent, language);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getSummary()).isEqualTo("이슈 없음");
    }

    private ChatResponse mockChatResponse(String content, int tokens) {
        AssistantMessage message = new AssistantMessage(content);

        Usage usage = mock(Usage.class);
        when(usage.getTotalTokens()).thenReturn(tokens);

        ChatGenerationMetadata metadata = mock(ChatGenerationMetadata.class);
        when(metadata.getUsage()).thenReturn(usage);

        Generation generation = new Generation(message, metadata);

        ChatResponse response = mock(ChatResponse.class);
        when(response.getResult()).thenReturn(generation);
        when(response.getMetadata()).thenReturn(mock(org.springframework.ai.chat.metadata.ChatResponseMetadata.class));
        when(response.getMetadata().getUsage()).thenReturn(usage);

        return response;
    }
}
