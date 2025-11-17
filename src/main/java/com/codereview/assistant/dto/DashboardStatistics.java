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
public class DashboardStatistics {

    private OverallStats overallStats;
    private Map<String, Integer> severityDistribution;
    private Map<String, Integer> categoryDistribution;
    private RecentActivity recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallStats {
        private Long totalRepositories;
        private Long totalPullRequests;
        private Long totalReviews;
        private Long totalComments;
        private Double averageCommentsPerReview;
        private Integer averageProcessingTimeMs;
        private Integer totalTokensUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private Integer reviewsToday;
        private Integer reviewsThisWeek;
        private Integer reviewsThisMonth;
        private LocalDateTime lastReviewTime;
    }
}
