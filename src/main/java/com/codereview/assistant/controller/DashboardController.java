package com.codereview.assistant.controller;

import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final StatisticsService statisticsService;

    /**
     * 대시보드 전체 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatistics> getDashboardStatistics() {
        log.info("GET /api/dashboard/statistics");
        DashboardStatistics statistics = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 최근 리뷰 목록 조회
     */
    @GetMapping("/reviews/recent")
    public ResponseEntity<List<ReviewSummaryDto>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /api/dashboard/reviews/recent?limit={}", limit);
        List<ReviewSummaryDto> reviews = statisticsService.getRecentReviews(limit);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 트렌드 데이터 조회
     */
    @GetMapping("/trends")
    public ResponseEntity<TrendDataDto> getTrendData(
            @RequestParam(defaultValue = "30") int days
    ) {
        log.info("GET /api/dashboard/trends?days={}", days);
        TrendDataDto trendData = statisticsService.getTrendData(days);
        return ResponseEntity.ok(trendData);
    }

    /**
     * Repository별 통계 조회
     */
    @GetMapping("/repositories/statistics")
    public ResponseEntity<Map<String, DashboardStatistics.OverallStats>> getRepositoryStatistics() {
        log.info("GET /api/dashboard/repositories/statistics");
        Map<String, DashboardStatistics.OverallStats> stats = statisticsService.getRepositoryStatistics();
        return ResponseEntity.ok(stats);
    }
}
