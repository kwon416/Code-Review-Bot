package com.codereview.assistant.service;

import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.domain.Repository;
import com.codereview.assistant.exception.ResourceNotFoundException;
import com.codereview.assistant.repository.PullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PullRequestService 테스트")
class PullRequestServiceTest {

    @Mock
    private PullRequestRepository pullRequestRepository;

    @InjectMocks
    private PullRequestService pullRequestService;

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
            .description("Test description")
            .author("testuser")
            .build();
    }

    @Test
    @DisplayName("PR 생성 성공")
    void createPullRequest_Success() {
        // Given
        when(pullRequestRepository.save(any(PullRequest.class))).thenReturn(testPullRequest);

        // When
        PullRequest result = pullRequestService.createPullRequest(testPullRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test PR");
        verify(pullRequestRepository).save(any(PullRequest.class));
    }

    @Test
    @DisplayName("PR 조회 성공")
    void getPullRequest_Success() {
        // Given
        when(pullRequestRepository.findById(1L)).thenReturn(Optional.of(testPullRequest));

        // When
        PullRequest result = pullRequestService.getPullRequest(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test PR");
    }

    @Test
    @DisplayName("PR 조회 실패 - 존재하지 않는 PR")
    void getPullRequest_NotFound() {
        // Given
        when(pullRequestRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pullRequestService.getPullRequest(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Pull request not found");
    }

    @Test
    @DisplayName("Repository와 PR 번호로 조회")
    void findByRepositoryAndPrNumber_Success() {
        // Given
        when(pullRequestRepository.findByRepositoryIdAndPrNumber(1L, 1))
            .thenReturn(Optional.of(testPullRequest));

        // When
        Optional<PullRequest> result = pullRequestService.findByRepositoryAndPrNumber(1L, 1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPrNumber()).isEqualTo(1);
    }
}
