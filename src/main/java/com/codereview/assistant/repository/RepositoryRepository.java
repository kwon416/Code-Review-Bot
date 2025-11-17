package com.codereview.assistant.repository;

import com.codereview.assistant.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByGithubId(Long githubId);

    Optional<Repository> findByOwnerAndName(String owner, String name);
}
