package com.codereview.assistant.repository;

import com.codereview.assistant.domain.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    Optional<PullRequest> findByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);

    List<PullRequest> findByRepositoryId(Long repositoryId);
}
