package com.codereview.assistant.service;

import com.codereview.assistant.domain.ReviewRule;
import com.codereview.assistant.dto.ReviewRuleDto;
import com.codereview.assistant.repository.ReviewRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRuleService 테스트")
class ReviewRuleServiceTest {

    @Mock
    private ReviewRuleRepository reviewRuleRepository;

    @InjectMocks
    private ReviewRuleService reviewRuleService;

    private ReviewRule testRule;

    @BeforeEach
    void setUp() {
        Map<String, Object> config = new HashMap<>();
        config.put("prompt", "Test prompt");

        testRule = ReviewRule.builder()
            .id(1L)
            .name("Test Rule")
            .description("Test Description")
            .ruleType("custom_prompt")
            .ruleConfig(config)
            .enabled(true)
            .priority(100)
            .targetFiles("**/*.java")
            .build();
    }

    @Test
    @DisplayName("Repository별 활성화된 규칙 조회 성공")
    void getActiveRulesForRepository_Success() {
        // Given
        Long repositoryId = 1L;
        when(reviewRuleRepository.findByRepositoryIdAndEnabledTrueOrderByPriorityDesc(repositoryId))
            .thenReturn(List.of(testRule));
        when(reviewRuleRepository.findByRepositoryIdIsNullAndEnabledTrueOrderByPriorityDesc())
            .thenReturn(List.of());

        // When
        List<ReviewRule> result = reviewRuleService.getActiveRulesForRepository(repositoryId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Rule");

        verify(reviewRuleRepository).findByRepositoryIdAndEnabledTrueOrderByPriorityDesc(repositoryId);
        verify(reviewRuleRepository).findByRepositoryIdIsNullAndEnabledTrueOrderByPriorityDesc();
    }

    @Test
    @DisplayName("파일 패턴에 맞는 규칙 필터링 성공")
    void getApplicableRules_MatchingPattern() {
        // Given
        String filePath = "src/main/java/Test.java";
        List<ReviewRule> rules = List.of(testRule);

        // When
        List<ReviewRule> result = reviewRuleService.getApplicableRules(rules, filePath);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("파일 패턴에 맞지 않는 규칙 필터링")
    void getApplicableRules_NonMatchingPattern() {
        // Given
        String filePath = "src/main/resources/config.yml";
        List<ReviewRule> rules = List.of(testRule);

        // When
        List<ReviewRule> result = reviewRuleService.getApplicableRules(rules, filePath);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("커스텀 프롬프트 생성 성공")
    void buildCustomPromptFromRules_Success() {
        // Given
        List<ReviewRule> rules = List.of(testRule);

        // When
        String result = reviewRuleService.buildCustomPromptFromRules(rules);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).contains("Test Rule");
        assertThat(result).contains("Test prompt");
    }

    @Test
    @DisplayName("규칙 생성 성공")
    void createRule_Success() {
        // Given
        Map<String, Object> config = new HashMap<>();
        config.put("prompt", "New rule prompt");

        ReviewRuleDto ruleDto = ReviewRuleDto.builder()
            .name("New Rule")
            .ruleType("custom_prompt")
            .ruleConfig(config)
            .enabled(true)
            .priority(50)
            .build();

        when(reviewRuleRepository.save(any(ReviewRule.class))).thenReturn(testRule);

        // When
        ReviewRuleDto result = reviewRuleService.createRule(ruleDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Rule");

        verify(reviewRuleRepository).save(any(ReviewRule.class));
    }

    @Test
    @DisplayName("규칙 수정 성공")
    void updateRule_Success() {
        // Given
        Long ruleId = 1L;
        ReviewRuleDto updateDto = ReviewRuleDto.builder()
            .name("Updated Rule")
            .enabled(false)
            .build();

        when(reviewRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(reviewRuleRepository.save(any(ReviewRule.class))).thenReturn(testRule);

        // When
        ReviewRuleDto result = reviewRuleService.updateRule(ruleId, updateDto);

        // Then
        assertThat(result).isNotNull();

        verify(reviewRuleRepository).findById(ruleId);
        verify(reviewRuleRepository).save(any(ReviewRule.class));
    }

    @Test
    @DisplayName("규칙 삭제 성공")
    void deleteRule_Success() {
        // Given
        Long ruleId = 1L;

        // When
        reviewRuleService.deleteRule(ruleId);

        // Then
        verify(reviewRuleRepository).deleteById(ruleId);
    }

    @Test
    @DisplayName("모든 규칙 조회 성공")
    void getAllRules_Success() {
        // Given
        when(reviewRuleRepository.findAll()).thenReturn(List.of(testRule));

        // When
        List<ReviewRuleDto> result = reviewRuleService.getAllRules(null);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        verify(reviewRuleRepository).findAll();
    }
}
