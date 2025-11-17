package com.codereview.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRuleDto {

    private Long id;
    private Long repositoryId;
    private String name;
    private String description;
    private String ruleType;
    private Map<String, Object> ruleConfig;
    private Boolean enabled;
    private Integer priority;
    private String targetFiles;
    private String excludeFiles;
    private String minSeverity;
    private String customMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
