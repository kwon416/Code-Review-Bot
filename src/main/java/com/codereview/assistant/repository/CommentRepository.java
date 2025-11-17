package com.codereview.assistant.repository;

import com.codereview.assistant.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByReviewId(Long reviewId);
}
