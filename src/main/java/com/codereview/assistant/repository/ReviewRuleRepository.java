package com.codereview.assistant.repository;

import com.codereview.assistant.domain.ReviewRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRuleRepository extends JpaRepository<ReviewRule, Long> {

    List<ReviewRule> findByRepositoryIdAndEnabledTrueOrderByPriorityDesc(Long repositoryId);

    List<ReviewRule> findByRepositoryIdOrderByPriorityDesc(Long repositoryId);

    List<ReviewRule> findByRepositoryIdIsNullAndEnabledTrueOrderByPriorityDesc();
}
