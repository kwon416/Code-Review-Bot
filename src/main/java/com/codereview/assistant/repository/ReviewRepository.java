package com.codereview.assistant.repository;

import com.codereview.assistant.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPullRequestId(Long pullRequestId);

    Optional<Review> findByPullRequestIdAndCommitSha(Long pullRequestId, String commitSha);
}
