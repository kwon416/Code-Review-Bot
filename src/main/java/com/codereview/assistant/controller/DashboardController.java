package com.codereview.assistant.controller;

import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard", description = "대시보드 통계 및 인사이트 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final StatisticsService statisticsService;

    @Operation(
        summary = "대시보드 전체 통계 조회",
        description = "총 Repository, PR, 리뷰, 코멘트 수 및 평균 통계, Severity/카테고리별 분포, 최근 활동 현황을 조회합니다."
    )
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatistics> getDashboardStatistics() {
        log.info("GET /api/dashboard/statistics");
        DashboardStatistics statistics = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "최근 리뷰 목록 조회",
        description = "최근 생성된 리뷰 목록을 조회합니다. 페이징을 지원합니다."
    )
    @GetMapping("/reviews/recent")
    public ResponseEntity<List<ReviewSummaryDto>> getRecentReviews(
            @Parameter(description = "조회할 리뷰 수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("GET /api/dashboard/reviews/recent?limit={}", limit);
        List<ReviewSummaryDto> reviews = statisticsService.getRecentReviews(limit);
        return ResponseEntity.ok(reviews);
    }

    @Operation(
        summary = "트렌드 데이터 조회",
        description = "지정된 기간 동안의 일별 리뷰, 코멘트, 이슈 트렌드를 조회합니다."
    )
    @GetMapping("/trends")
    public ResponseEntity<TrendDataDto> getTrendData(
            @Parameter(description = "조회할 일수", example = "30")
            @RequestParam(defaultValue = "30") int days
    ) {
        log.info("GET /api/dashboard/trends?days={}", days);
        TrendDataDto trendData = statisticsService.getTrendData(days);
        return ResponseEntity.ok(trendData);
    }

    @Operation(
        summary = "Repository별 통계 조회",
        description = "각 Repository별 리뷰 통계를 조회합니다."
    )
    @GetMapping("/repositories/statistics")
    public ResponseEntity<Map<String, DashboardStatistics.OverallStats>> getRepositoryStatistics() {
        log.info("GET /api/dashboard/repositories/statistics");
        Map<String, DashboardStatistics.OverallStats> stats = statisticsService.getRepositoryStatistics();
        return ResponseEntity.ok(stats);
    }
}
