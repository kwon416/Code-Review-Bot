package com.codereview.assistant.service;

import com.codereview.assistant.domain.Comment;
import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.domain.Repository;
import com.codereview.assistant.domain.Review;
import com.codereview.assistant.exception.ResourceNotFoundException;
import com.codereview.assistant.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private PullRequest testPullRequest;
    private Repository testRepository;

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
            .build();

        testReview = Review.builder()
            .id(1L)
            .pullRequest(testPullRequest)
            .commitSha("abc123")
            .reviewStatus("completed")
            .totalComments(5)
            .aiModel("gpt-4")
            .tokensUsed(1000)
            .processingTimeMs(5000L)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_Success() {
        // Given
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        Review result = reviewService.createReview(testReview);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReviewStatus()).isEqualTo("completed");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 조회 성공")
    void getReview_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When
        Review result = reviewService.getReview(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCommitSha()).isEqualTo("abc123");
        verify(reviewRepository).findById(1L);
    }

    @Test
    @DisplayName("리뷰 조회 실패 - 존재하지 않는 리뷰")
    void getReview_NotFound() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReview(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Review not found");
    }

    @Test
    @DisplayName("PR별 리뷰 목록 조회")
    void getReviewsByPullRequest_Success() {
        // Given
        when(reviewRepository.findByPullRequestId(1L)).thenReturn(List.of(testReview));

        // When
        List<Review> results = reviewService.getReviewsByPullRequest(1L);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPullRequest().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("리뷰 상태 업데이트")
    void updateReviewStatus_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        reviewService.updateReviewStatus(1L, "in_progress");

        // Then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }
}
