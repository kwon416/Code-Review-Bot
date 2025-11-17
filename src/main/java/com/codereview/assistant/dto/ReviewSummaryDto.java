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
public class ReviewSummaryDto {

    private Long reviewId;
    private String repositoryName;
    private String repositoryOwner;
    private Integer prNumber;
    private String prTitle;
    private String commitSha;
    private String reviewStatus;
    private Integer totalComments;
    private Map<String, Integer> severityCounts;
    private Integer tokensUsed;
    private Integer processingTimeMs;
    private LocalDateTime createdAt;
}
