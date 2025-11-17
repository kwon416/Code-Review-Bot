package com.codereview.assistant.service;

import com.codereview.assistant.domain.Comment;
import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.domain.Repository;
import com.codereview.assistant.domain.Review;
import com.codereview.assistant.dto.DashboardStatistics;
import com.codereview.assistant.dto.ReviewSummaryDto;
import com.codereview.assistant.dto.TrendDataDto;
import com.codereview.assistant.repository.CommentRepository;
import com.codereview.assistant.repository.PullRequestRepository;
import com.codereview.assistant.repository.RepositoryRepository;
import com.codereview.assistant.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsService 테스트")
class StatisticsServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private Repository testRepository;
    private PullRequest testPullRequest;
    private Review testReview;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testRepository = Repository.builder()
            .id(1L)
            .githubId(12345L)
            .owner("testowner")
            .name("testrepo")
            .build();

        testPullRequest = PullRequest.builder()
            .id(1L)
            .repository(testRepository)
            .prNumber(1)
            .title("Test PR")
            .author("testuser")
            .status("open")
            .build();

        Map<String, Integer> severityCounts = new HashMap<>();
        severityCounts.put("info", 1);
        severityCounts.put("warning", 2);
        severityCounts.put("error", 1);

        testReview = Review.builder()
            .id(1L)
            .pullRequest(testPullRequest)
            .commitSha("abc123")
            .reviewStatus("completed")
            .totalComments(4)
            .severityCounts(severityCounts)
            .tokensUsed(1000)
            .processingTimeMs(5000)
            .createdAt(LocalDateTime.now())
            .build();

        testComment = Comment.builder()
            .id(1L)
            .review(testReview)
            .filePath("src/main/Test.java")
            .lineNumber(10)
            .severity("warning")
            .category("performance")
            .message("Test issue")
            .build();
    }

    @Test
    @DisplayName("대시보드 전체 통계 조회 성공")
    void getDashboardStatistics_Success() {
        // Given
        when(repositoryRepository.count()).thenReturn(5L);
        when(pullRequestRepository.count()).thenReturn(20L);
        when(reviewRepository.count()).thenReturn(30L);
        when(commentRepository.count()).thenReturn(100L);
        when(reviewRepository.findByReviewStatus("completed"))
            .thenReturn(List.of(testReview));
        when(commentRepository.findAll()).thenReturn(List.of(testComment));
        when(reviewRepository.findByCreatedAtAfter(any())).thenReturn(List.of(testReview));
        when(reviewRepository.findFirstByOrderByCreatedAtDesc())
            .thenReturn(Optional.of(testReview));

        // When
        DashboardStatistics result = statisticsService.getDashboardStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOverallStats()).isNotNull();
        assertThat(result.getOverallStats().getTotalRepositories()).isEqualTo(5L);
        assertThat(result.getOverallStats().getTotalPullRequests()).isEqualTo(20L);
        assertThat(result.getOverallStats().getTotalReviews()).isEqualTo(30L);
        assertThat(result.getOverallStats().getTotalComments()).isEqualTo(100L);
        assertThat(result.getSeverityDistribution()).containsKey("warning");
        assertThat(result.getCategoryDistribution()).containsKey("performance");
        assertThat(result.getRecentActivity()).isNotNull();

        verify(repositoryRepository).count();
        verify(pullRequestRepository).count();
        verify(reviewRepository).count();
        verify(commentRepository).count();
    }

    @Test
    @DisplayName("최근 리뷰 목록 조회 성공")
    void getRecentReviews_Success() {
        // Given
        int limit = 10;
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview));
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(reviewPage);

        // When
        List<ReviewSummaryDto> result = statisticsService.getRecentReviews(limit);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReviewId()).isEqualTo(testReview.getId());
        assertThat(result.get(0).getRepositoryName()).isEqualTo("testrepo");
        assertThat(result.get(0).getPrNumber()).isEqualTo(1);

        verify(reviewRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("트렌드 데이터 조회 성공")
    void getTrendData_Success() {
        // Given
        int days = 7;
        when(reviewRepository.findByCreatedAtAfter(any())).thenReturn(List.of(testReview));

        // When
        TrendDataDto result = statisticsService.getTrendData(days);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDailyReviews()).isNotNull();
        assertThat(result.getDailyComments()).isNotNull();
        assertThat(result.getDailyIssues()).isNotNull();

        verify(reviewRepository).findByCreatedAtAfter(any());
    }

    @Test
    @DisplayName("Repository별 통계 조회 성공")
    void getRepositoryStatistics_Success() {
        // Given
        when(repositoryRepository.findAll()).thenReturn(List.of(testRepository));
        when(pullRequestRepository.findByRepositoryId(1L))
            .thenReturn(List.of(testPullRequest));
        when(reviewRepository.findByPullRequestId(1L))
            .thenReturn(List.of(testReview));

        // When
        Map<String, DashboardStatistics.OverallStats> result =
            statisticsService.getRepositoryStatistics();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).containsKey("testowner/testrepo");

        verify(repositoryRepository).findAll();
        verify(pullRequestRepository).findByRepositoryId(1L);
    }
}
