package com.codereview.assistant.controller;

import com.codereview.assistant.domain.Comment;
import com.codereview.assistant.domain.Review;
import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.exception.ResourceNotFoundException;
import com.codereview.assistant.repository.ReviewRepository;
import com.codereview.assistant.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Dashboard", description = "대시보드 통계 및 인사이트 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final StatisticsService statisticsService;
    private final ReviewRepository reviewRepository;

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

    @Operation(
        summary = "리뷰 상세 정보 조회",
        description = "특정 리뷰의 상세 정보 및 코멘트 목록을 조회합니다."
    )
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<Map<String, Object>> getReviewDetail(
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId
    ) {
        log.info("GET /api/dashboard/reviews/{}", reviewId);

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", review.getId());
        result.put("commitSha", review.getCommitSha());
        result.put("reviewStatus", review.getReviewStatus());
        result.put("totalComments", review.getTotalComments());
        result.put("severityCounts", review.getSeverityCounts());
        result.put("aiModel", review.getAiModel());
        result.put("tokensUsed", review.getTokensUsed());
        result.put("processingTimeMs", review.getProcessingTimeMs());
        result.put("createdAt", review.getCreatedAt());
        result.put("errorMessage", review.getErrorMessage());

        // Pull Request 정보
        Map<String, Object> prInfo = new HashMap<>();
        prInfo.put("number", review.getPullRequest().getPrNumber());
        prInfo.put("title", review.getPullRequest().getTitle());
        prInfo.put("author", review.getPullRequest().getAuthor());
        prInfo.put("description", review.getPullRequest().getDescription());
        result.put("pullRequest", prInfo);

        // Repository 정보
        Map<String, Object> repoInfo = new HashMap<>();
        repoInfo.put("owner", review.getPullRequest().getRepository().getOwner());
        repoInfo.put("name", review.getPullRequest().getRepository().getName());
        result.put("repository", repoInfo);

        // 코멘트 목록
        List<Map<String, Object>> comments = review.getComments().stream()
            .map(comment -> {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("filePath", comment.getFilePath());
                commentMap.put("lineNumber", comment.getLineNumber());
                commentMap.put("severity", comment.getSeverity());
                commentMap.put("category", comment.getCategory());
                commentMap.put("message", comment.getMessage());
                commentMap.put("suggestion", comment.getSuggestion());
                commentMap.put("codeExample", comment.getCodeExample());
                commentMap.put("createdAt", comment.getCreatedAt());
                return commentMap;
            })
            .collect(Collectors.toList());
        result.put("comments", comments);

        return ResponseEntity.ok(result);
    }
}
