package com.codereview.assistant.service;

import com.codereview.assistant.domain.Comment;
import com.codereview.assistant.domain.Review;
import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.repository.CommentRepository;
import com.codereview.assistant.repository.PullRequestRepository;
import com.codereview.assistant.repository.RepositoryRepository;
import com.codereview.assistant.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final RepositoryRepository repositoryRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    /**
     * 대시보드 전체 통계 조회
     */
    @Transactional(readOnly = true)
    public DashboardStatistics getDashboardStatistics() {
        log.info("Fetching dashboard statistics");

        // 전체 통계
        DashboardStatistics.OverallStats overallStats = calculateOverallStats();

        // Severity 분포
        Map<String, Integer> severityDistribution = calculateSeverityDistribution();

        // 카테고리 분포
        Map<String, Integer> categoryDistribution = calculateCategoryDistribution();

        // 최근 활동
        DashboardStatistics.RecentActivity recentActivity = calculateRecentActivity();

        return DashboardStatistics.builder()
            .overallStats(overallStats)
            .severityDistribution(severityDistribution)
            .categoryDistribution(categoryDistribution)
            .recentActivity(recentActivity)
            .build();
    }

    /**
     * 최근 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewSummaryDto> getRecentReviews(int limit) {
        log.info("Fetching recent {} reviews", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Review> reviews = reviewRepository.findAll(pageable).getContent();

        return reviews.stream()
            .map(this::convertToReviewSummary)
            .collect(Collectors.toList());
    }

    /**
     * 트렌드 데이터 조회 (최근 30일)
     */
    @Transactional(readOnly = true)
    public TrendDataDto getTrendData(int days) {
        log.info("Fetching trend data for last {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Review> reviews = reviewRepository.findByCreatedAtAfter(startDate);

        // 날짜별 리뷰 수
        List<TrendDataDto.DataPoint> dailyReviews = calculateDailyReviews(reviews, days);

        // 날짜별 코멘트 수
        List<TrendDataDto.DataPoint> dailyComments = calculateDailyComments(reviews, days);

        // 날짜별 이슈 수 (error severity)
        List<TrendDataDto.DataPoint> dailyIssues = calculateDailyIssues(reviews, days);

        return TrendDataDto.builder()
            .dailyReviews(dailyReviews)
            .dailyComments(dailyComments)
            .dailyIssues(dailyIssues)
            .build();
    }

    /**
     * Repository별 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, DashboardStatistics.OverallStats> getRepositoryStatistics() {
        log.info("Fetching repository statistics");

        Map<String, DashboardStatistics.OverallStats> stats = new HashMap<>();

        repositoryRepository.findAll().forEach(repo -> {
            String repoKey = repo.getOwner() + "/" + repo.getName();
            stats.put(repoKey, calculateRepositoryStats(repo.getId()));
        });

        return stats;
    }

    private DashboardStatistics.OverallStats calculateOverallStats() {
        long totalRepos = repositoryRepository.count();
        long totalPrs = pullRequestRepository.count();
        long totalReviews = reviewRepository.count();
        long totalComments = commentRepository.count();

        List<Review> completedReviews = reviewRepository.findByReviewStatus("completed");

        double avgComments = completedReviews.isEmpty() ? 0.0 :
            completedReviews.stream()
                .mapToInt(Review::getTotalComments)
                .average()
                .orElse(0.0);

        int avgProcessingTime = completedReviews.isEmpty() ? 0 :
            (int) completedReviews.stream()
                .filter(r -> r.getProcessingTimeMs() != null)
                .mapToInt(Review::getProcessingTimeMs)
                .average()
                .orElse(0.0);

        int totalTokens = completedReviews.stream()
            .filter(r -> r.getTokensUsed() != null)
            .mapToInt(Review::getTokensUsed)
            .sum();

        return DashboardStatistics.OverallStats.builder()
            .totalRepositories(totalRepos)
            .totalPullRequests(totalPrs)
            .totalReviews(totalReviews)
            .totalComments(totalComments)
            .averageCommentsPerReview(avgComments)
            .averageProcessingTimeMs(avgProcessingTime)
            .totalTokensUsed(totalTokens)
            .build();
    }

    private Map<String, Integer> calculateSeverityDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("info", 0);
        distribution.put("warning", 0);
        distribution.put("error", 0);

        List<Comment> allComments = commentRepository.findAll();
        for (Comment comment : allComments) {
            distribution.merge(comment.getSeverity(), 1, Integer::sum);
        }

        return distribution;
    }

    private Map<String, Integer> calculateCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        List<Comment> allComments = commentRepository.findAll();
        for (Comment comment : allComments) {
            distribution.merge(comment.getCategory(), 1, Integer::sum);
        }

        return distribution;
    }

    private DashboardStatistics.RecentActivity calculateRecentActivity() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        int reviewsToday = reviewRepository.findByCreatedAtAfter(startOfToday).size();
        int reviewsThisWeek = reviewRepository.findByCreatedAtAfter(startOfWeek).size();
        int reviewsThisMonth = reviewRepository.findByCreatedAtAfter(startOfMonth).size();

        LocalDateTime lastReviewTime = reviewRepository.findFirstByOrderByCreatedAtDesc()
            .map(Review::getCreatedAt)
            .orElse(null);

        return DashboardStatistics.RecentActivity.builder()
            .reviewsToday(reviewsToday)
            .reviewsThisWeek(reviewsThisWeek)
            .reviewsThisMonth(reviewsThisMonth)
            .lastReviewTime(lastReviewTime)
            .build();
    }

    private DashboardStatistics.OverallStats calculateRepositoryStats(Long repoId) {
        List<Review> reviews = pullRequestRepository.findByRepositoryId(repoId).stream()
            .flatMap(pr -> reviewRepository.findByPullRequestId(pr.getId()).stream())
            .collect(Collectors.toList());

        long totalComments = reviews.stream()
            .mapToInt(Review::getTotalComments)
            .sum();

        double avgComments = reviews.isEmpty() ? 0.0 :
            reviews.stream()
                .mapToInt(Review::getTotalComments)
                .average()
                .orElse(0.0);

        return DashboardStatistics.OverallStats.builder()
            .totalReviews((long) reviews.size())
            .totalComments(totalComments)
            .averageCommentsPerReview(avgComments)
            .build();
    }

    private ReviewSummaryDto convertToReviewSummary(Review review) {
        return ReviewSummaryDto.builder()
            .reviewId(review.getId())
            .repositoryName(review.getPullRequest().getRepository().getName())
            .repositoryOwner(review.getPullRequest().getRepository().getOwner())
            .prNumber(review.getPullRequest().getPrNumber())
            .prTitle(review.getPullRequest().getTitle())
            .commitSha(review.getCommitSha())
            .reviewStatus(review.getReviewStatus())
            .totalComments(review.getTotalComments())
            .severityCounts(review.getSeverityCounts())
            .tokensUsed(review.getTokensUsed())
            .processingTimeMs(review.getProcessingTimeMs())
            .createdAt(review.getCreatedAt())
            .build();
    }

    private List<TrendDataDto.DataPoint> calculateDailyReviews(List<Review> reviews, int days) {
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();

        // Initialize all dates
        for (int i = 0; i < days; i++) {
            dailyCounts.put(LocalDate.now().minusDays(i), 0);
        }

        // Count reviews per day
        reviews.forEach(review -> {
            LocalDate date = review.getCreatedAt().toLocalDate();
            dailyCounts.merge(date, 1, Integer::sum);
        });

        return dailyCounts.entrySet().stream()
            .map(entry -> TrendDataDto.DataPoint.builder()
                .date(entry.getKey())
                .count(entry.getValue())
                .build())
            .sorted(Comparator.comparing(TrendDataDto.DataPoint::getDate))
            .collect(Collectors.toList());
    }

    private List<TrendDataDto.DataPoint> calculateDailyComments(List<Review> reviews, int days) {
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();

        for (int i = 0; i < days; i++) {
            dailyCounts.put(LocalDate.now().minusDays(i), 0);
        }

        reviews.forEach(review -> {
            LocalDate date = review.getCreatedAt().toLocalDate();
            dailyCounts.merge(date, review.getTotalComments(), Integer::sum);
        });

        return dailyCounts.entrySet().stream()
            .map(entry -> TrendDataDto.DataPoint.builder()
                .date(entry.getKey())
                .count(entry.getValue())
                .build())
            .sorted(Comparator.comparing(TrendDataDto.DataPoint::getDate))
            .collect(Collectors.toList());
    }

    private List<TrendDataDto.DataPoint> calculateDailyIssues(List<Review> reviews, int days) {
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();

        for (int i = 0; i < days; i++) {
            dailyCounts.put(LocalDate.now().minusDays(i), 0);
        }

        reviews.forEach(review -> {
            LocalDate date = review.getCreatedAt().toLocalDate();
            Map<String, Integer> counts = review.getSeverityCounts();
            if (counts != null && counts.containsKey("error")) {
                dailyCounts.merge(date, counts.get("error"), Integer::sum);
            }
        });

        return dailyCounts.entrySet().stream()
            .map(entry -> TrendDataDto.DataPoint.builder()
                .date(entry.getKey())
                .count(entry.getValue())
                .build())
            .sorted(Comparator.comparing(TrendDataDto.DataPoint::getDate))
            .collect(Collectors.toList());
    }
}
