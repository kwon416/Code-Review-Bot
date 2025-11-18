package com.codereview.assistant.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequest pullRequest;

    @Column(name = "commit_sha", length = 40, nullable = false)
    private String commitSha;

    @Column(name = "review_status", length = 50)
    @Builder.Default
    private String reviewStatus = "pending";

    @Column(name = "total_comments")
    @Builder.Default
    private Integer totalComments = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "severity_counts")
    private Map<String, Integer> severityCounts;

    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
