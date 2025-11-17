package com.codereview.assistant.service;

import com.codereview.assistant.domain.ReviewRule;
import com.codereview.assistant.dto.ReviewRuleDto;
import com.codereview.assistant.exception.ResourceNotFoundException;
import com.codereview.assistant.repository.ReviewRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewRuleService {

    private final ReviewRuleRepository reviewRuleRepository;

    /**
     * Repository별 활성화된 규칙 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewRule> getActiveRulesForRepository(Long repositoryId) {
        log.info("Getting active rules for repository: {}", repositoryId);

        // Repository 전용 규칙 + 전역 규칙
        List<ReviewRule> repositoryRules = reviewRuleRepository
            .findByRepositoryIdAndEnabledTrueOrderByPriorityDesc(repositoryId);

        List<ReviewRule> globalRules = reviewRuleRepository
            .findByRepositoryIdIsNullAndEnabledTrueOrderByPriorityDesc();

        repositoryRules.addAll(globalRules);
        return repositoryRules;
    }

    /**
     * 특정 파일에 적용 가능한 규칙 필터링
     */
    public List<ReviewRule> getApplicableRules(List<ReviewRule> rules, String filePath) {
        return rules.stream()
            .filter(rule -> isRuleApplicableToFile(rule, filePath))
            .collect(Collectors.toList());
    }

    /**
     * 규칙이 특정 파일에 적용 가능한지 확인
     */
    private boolean isRuleApplicableToFile(ReviewRule rule, String filePath) {
        // targetFiles 체크
        if (rule.getTargetFiles() != null && !rule.getTargetFiles().isEmpty()) {
            if (!matchesPattern(filePath, rule.getTargetFiles())) {
                return false;
            }
        }

        // excludeFiles 체크
        if (rule.getExcludeFiles() != null && !rule.getExcludeFiles().isEmpty()) {
            if (matchesPattern(filePath, rule.getExcludeFiles())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Glob 패턴 매칭
     */
    private boolean matchesPattern(String filePath, String pattern) {
        try {
            String[] patterns = pattern.split(",");
            for (String p : patterns) {
                PathMatcher matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + p.trim());
                if (matcher.matches(java.nio.file.Paths.get(filePath))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Error matching pattern {} for file {}: {}", pattern, filePath, e.getMessage());
            return false;
        }
    }

    /**
     * 커스텀 규칙을 AI 프롬프트에 추가
     */
    public String buildCustomPromptFromRules(List<ReviewRule> rules) {
        if (rules.isEmpty()) {
            return "";
        }

        StringBuilder customPrompt = new StringBuilder();
        customPrompt.append("\n\nAdditional Custom Rules:\n");

        for (ReviewRule rule : rules) {
            customPrompt.append("- ").append(rule.getName()).append(": ");

            if (rule.getRuleType().equals("custom_prompt")) {
                Object prompt = rule.getRuleConfig().get("prompt");
                if (prompt != null) {
                    customPrompt.append(prompt.toString());
                }
            } else if (rule.getRuleType().equals("code_pattern")) {
                customPrompt.append("Check for patterns: ");
                Object patterns = rule.getRuleConfig().get("patterns");
                if (patterns != null) {
                    customPrompt.append(patterns.toString());
                }
            }

            if (rule.getCustomMessage() != null) {
                customPrompt.append(" (").append(rule.getCustomMessage()).append(")");
            }

            customPrompt.append("\n");
        }

        return customPrompt.toString();
    }

    /**
     * 규칙 생성
     */
    @Transactional
    public ReviewRuleDto createRule(ReviewRuleDto ruleDto) {
        ReviewRule rule = ReviewRule.builder()
            .name(ruleDto.getName())
            .description(ruleDto.getDescription())
            .ruleType(ruleDto.getRuleType())
            .ruleConfig(ruleDto.getRuleConfig())
            .enabled(ruleDto.getEnabled() != null ? ruleDto.getEnabled() : true)
            .priority(ruleDto.getPriority() != null ? ruleDto.getPriority() : 0)
            .targetFiles(ruleDto.getTargetFiles())
            .excludeFiles(ruleDto.getExcludeFiles())
            .minSeverity(ruleDto.getMinSeverity())
            .customMessage(ruleDto.getCustomMessage())
            .build();

        rule = reviewRuleRepository.save(rule);
        return convertToDto(rule);
    }

    /**
     * 규칙 수정
     */
    @Transactional
    public ReviewRuleDto updateRule(Long id, ReviewRuleDto ruleDto) {
        ReviewRule rule = reviewRuleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ReviewRule", "id", id));

        if (ruleDto.getName() != null) rule.setName(ruleDto.getName());
        if (ruleDto.getDescription() != null) rule.setDescription(ruleDto.getDescription());
        if (ruleDto.getRuleType() != null) rule.setRuleType(ruleDto.getRuleType());
        if (ruleDto.getRuleConfig() != null) rule.setRuleConfig(ruleDto.getRuleConfig());
        if (ruleDto.getEnabled() != null) rule.setEnabled(ruleDto.getEnabled());
        if (ruleDto.getPriority() != null) rule.setPriority(ruleDto.getPriority());
        if (ruleDto.getTargetFiles() != null) rule.setTargetFiles(ruleDto.getTargetFiles());
        if (ruleDto.getExcludeFiles() != null) rule.setExcludeFiles(ruleDto.getExcludeFiles());
        if (ruleDto.getMinSeverity() != null) rule.setMinSeverity(ruleDto.getMinSeverity());
        if (ruleDto.getCustomMessage() != null) rule.setCustomMessage(ruleDto.getCustomMessage());

        rule = reviewRuleRepository.save(rule);
        return convertToDto(rule);
    }

    /**
     * 규칙 삭제
     */
    @Transactional
    public void deleteRule(Long id) {
        reviewRuleRepository.deleteById(id);
    }

    /**
     * 모든 규칙 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewRuleDto> getAllRules(Long repositoryId) {
        List<ReviewRule> rules;
        if (repositoryId != null) {
            rules = reviewRuleRepository.findByRepositoryIdOrderByPriorityDesc(repositoryId);
        } else {
            rules = reviewRuleRepository.findAll();
        }

        return rules.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private ReviewRuleDto convertToDto(ReviewRule rule) {
        return ReviewRuleDto.builder()
            .id(rule.getId())
            .repositoryId(rule.getRepository() != null ? rule.getRepository().getId() : null)
            .name(rule.getName())
            .description(rule.getDescription())
            .ruleType(rule.getRuleType())
            .ruleConfig(rule.getRuleConfig())
            .enabled(rule.getEnabled())
            .priority(rule.getPriority())
            .targetFiles(rule.getTargetFiles())
            .excludeFiles(rule.getExcludeFiles())
            .minSeverity(rule.getMinSeverity())
            .customMessage(rule.getCustomMessage())
            .createdAt(rule.getCreatedAt())
            .updatedAt(rule.getUpdatedAt())
            .build();
    }
}
