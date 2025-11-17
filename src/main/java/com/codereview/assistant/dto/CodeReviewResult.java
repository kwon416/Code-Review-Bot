package com.codereview.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewResult {

    private List<ReviewComment> comments;
    private String summary;
    private int tokensUsed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewComment {
        private String filePath;
        private Integer lineNumber;
        private String severity; // info, warning, error
        private String category; // bug, performance, security, style, best-practice
        private String message;
        private String suggestion;
        private String codeExample;
    }
}
