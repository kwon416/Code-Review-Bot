package com.codereview.assistant.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "review_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType; // file_pattern, code_pattern, complexity, custom_prompt

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rule_config", nullable = false)
    private Map<String, Object> ruleConfig;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "target_files", columnDefinition = "TEXT")
    private String targetFiles; // Glob pattern (e.g., "**/*.java", "src/**/*.ts")

    @Column(name = "exclude_files", columnDefinition = "TEXT")
    private String excludeFiles; // Glob pattern for exclusions

    @Column(name = "min_severity", length = 20)
    private String minSeverity; // info, warning, error

    @Column(name = "custom_message", columnDefinition = "TEXT")
    private String customMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
