package com.codereview.assistant.controller;

import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController 통합 테스트")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @DisplayName("GET /api/dashboard/statistics - 성공")
    void getDashboardStatistics_Success() throws Exception {
        // Given
        DashboardStatistics.OverallStats overallStats = DashboardStatistics.OverallStats.builder()
            .totalRepositories(5L)
            .totalPullRequests(20L)
            .totalReviews(30L)
            .totalComments(100L)
            .averageCommentsPerReview(3.3)
            .averageProcessingTimeMs(5000)
            .totalTokensUsed(10000)
            .build();

        DashboardStatistics.RecentActivity recentActivity = DashboardStatistics.RecentActivity.builder()
            .reviewsToday(5)
            .reviewsThisWeek(15)
            .reviewsThisMonth(30)
            .lastReviewTime(LocalDateTime.now())
            .build();

        DashboardStatistics statistics = DashboardStatistics.builder()
            .overallStats(overallStats)
            .severityDistribution(Map.of("info", 50, "warning", 30, "error", 20))
            .categoryDistribution(Map.of("bug", 40, "performance", 30, "security", 30))
            .recentActivity(recentActivity)
            .build();

        when(statisticsService.getDashboardStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/dashboard/statistics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overallStats.totalRepositories").value(5))
            .andExpect(jsonPath("$.overallStats.totalPullRequests").value(20))
            .andExpect(jsonPath("$.overallStats.totalReviews").value(30))
            .andExpect(jsonPath("$.severityDistribution.info").value(50))
            .andExpect(jsonPath("$.categoryDistribution.bug").value(40));
    }

    @Test
    @DisplayName("GET /api/dashboard/reviews/recent - 성공")
    void getRecentReviews_Success() throws Exception {
        // Given
        ReviewSummaryDto review = ReviewSummaryDto.builder()
            .reviewId(1L)
            .repositoryName("testrepo")
            .repositoryOwner("testowner")
            .prNumber(1)
            .prTitle("Test PR")
            .commitSha("abc123")
            .reviewStatus("completed")
            .totalComments(5)
            .tokensUsed(1000)
            .processingTimeMs(5000)
            .createdAt(LocalDateTime.now())
            .build();

        when(statisticsService.getRecentReviews(anyInt())).thenReturn(List.of(review));

        // When & Then
        mockMvc.perform(get("/api/dashboard/reviews/recent")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reviewId").value(1))
            .andExpect(jsonPath("$[0].repositoryName").value("testrepo"))
            .andExpect(jsonPath("$[0].prNumber").value(1));
    }

    @Test
    @DisplayName("GET /api/dashboard/trends - 성공")
    void getTrendData_Success() throws Exception {
        // Given
        TrendDataDto trendData = TrendDataDto.builder()
            .dailyReviews(List.of())
            .dailyComments(List.of())
            .dailyIssues(List.of())
            .build();

        when(statisticsService.getTrendData(anyInt())).thenReturn(trendData);

        // When & Then
        mockMvc.perform(get("/api/dashboard/trends")
                .param("days", "30")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dailyReviews").isArray())
            .andExpect(jsonPath("$.dailyComments").isArray())
            .andExpect(jsonPath("$.dailyIssues").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/repositories/statistics - 성공")
    void getRepositoryStatistics_Success() throws Exception {
        // Given
        DashboardStatistics.OverallStats stats = DashboardStatistics.OverallStats.builder()
            .totalReviews(10L)
            .totalComments(50L)
            .averageCommentsPerReview(5.0)
            .build();

        when(statisticsService.getRepositoryStatistics())
            .thenReturn(Map.of("testowner/testrepo", stats));

        // When & Then
        mockMvc.perform(get("/api/dashboard/repositories/statistics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['testowner/testrepo'].totalReviews").value(10))
            .andExpect(jsonPath("$['testowner/testrepo'].totalComments").value(50));
    }
}
