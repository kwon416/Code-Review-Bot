package com.codereview.assistant.service;

import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.domain.Repository;
import com.codereview.assistant.dto.GitHubWebhookPayload;
import com.codereview.assistant.repository.PullRequestRepository;
import com.codereview.assistant.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;

    @Transactional
    public PullRequest handlePullRequestEvent(GitHubWebhookPayload payload) {
        log.info("Handling pull request event: action={}, pr_number={}",
            payload.getAction(), payload.getPullRequest().getNumber());

        // Get or create repository
        Repository repository = getOrCreateRepository(payload);

        // Get or create pull request
        PullRequest pullRequest = pullRequestRepository
            .findByRepositoryIdAndPrNumber(repository.getId(), payload.getPullRequest().getNumber())
            .orElseGet(() -> createPullRequest(repository, payload));

        // Update pull request details
        pullRequest.setTitle(payload.getPullRequest().getTitle());
        pullRequest.setDescription(payload.getPullRequest().getBody());
        pullRequest.setStatus(payload.getPullRequest().getState());

        return pullRequestRepository.save(pullRequest);
    }

    private Repository getOrCreateRepository(GitHubWebhookPayload payload) {
        return repositoryRepository
            .findByGithubId(payload.getRepository().getId())
            .orElseGet(() -> {
                Repository newRepo = Repository.builder()
                    .githubId(payload.getRepository().getId())
                    .owner(payload.getRepository().getOwner().getLogin())
                    .name(payload.getRepository().getName())
                    .installationId(payload.getInstallation() != null ?
                        payload.getInstallation().getId() : null)
                    .build();
                return repositoryRepository.save(newRepo);
            });
    }

    private PullRequest createPullRequest(Repository repository, GitHubWebhookPayload payload) {
        return PullRequest.builder()
            .repository(repository)
            .prNumber(payload.getPullRequest().getNumber())
            .title(payload.getPullRequest().getTitle())
            .description(payload.getPullRequest().getBody())
            .author(payload.getPullRequest().getUser().getLogin())
            .status(payload.getPullRequest().getState())
            .build();
    }
}
