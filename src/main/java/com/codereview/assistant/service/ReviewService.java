package com.codereview.assistant.service;

import com.codereview.assistant.domain.Comment;
import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.domain.Review;
import com.codereview.assistant.domain.ReviewRule;
import com.codereview.assistant.dto.CodeReviewResult;
import com.codereview.assistant.repository.CommentRepository;
import com.codereview.assistant.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final CodeReviewService codeReviewService;
    private final GitHubClientService gitHubClientService;
    private final ReviewRuleService reviewRuleService;

    @Async
    @Transactional
    public void performReview(PullRequest pullRequest, String commitSha) {
        log.info("Starting review for PR #{} at commit {}", pullRequest.getPrNumber(), commitSha);

        long startTime = System.currentTimeMillis();

        try {
            // Create review record
            Review review = Review.builder()
                .pullRequest(pullRequest)
                .commitSha(commitSha)
                .reviewStatus("in_progress")
                .aiModel("gpt-4o-mini")
                .build();
            review = reviewRepository.save(review);
            final Review savedReview = review;  // Make it effectively final for lambda

            // Fetch PR diff from GitHub
            String diff = gitHubClientService.getPullRequestDiff(
                pullRequest.getRepository().getOwner(),
                pullRequest.getRepository().getName(),
                pullRequest.getPrNumber(),
                pullRequest.getRepository().getInstallationId()
            );

            // Get custom review rules for this repository
            List<ReviewRule> customRules = reviewRuleService
                .getActiveRulesForRepository(pullRequest.getRepository().getId());

            // Analyze code with AI (with custom rules if available)
            CodeReviewResult result;
            if (customRules.isEmpty()) {
                result = codeReviewService.analyzeCode(diff, detectLanguage(diff));
            } else {
                result = codeReviewService.analyzeCodeWithRules(diff, detectLanguage(diff), customRules);
            }

            // Save comments to database
            List<Comment> comments = result.getComments().stream()
                .map(rc -> Comment.builder()
                    .review(savedReview)
                    .filePath(rc.getFilePath())
                    .lineNumber(rc.getLineNumber())
                    .severity(rc.getSeverity())
                    .category(rc.getCategory())
                    .message(rc.getMessage())
                    .suggestion(rc.getSuggestion())
                    .codeExample(rc.getCodeExample())
                    .build())
                .collect(Collectors.toList());

            commentRepository.saveAll(comments);

            // Post comments to GitHub
            List<GitHubClientService.ReviewCommentRequest> githubComments =
                result.getComments().stream()
                    .map(rc -> GitHubClientService.ReviewCommentRequest.builder()
                        .filePath(rc.getFilePath())
                        .lineNumber(rc.getLineNumber())
                        .severity(rc.getSeverity())
                        .category(rc.getCategory())
                        .message(rc.getMessage())
                        .suggestion(rc.getSuggestion())
                        .codeExample(rc.getCodeExample())
                        .build())
                    .collect(Collectors.toList());

            gitHubClientService.postReviewComments(
                pullRequest.getRepository().getOwner(),
                pullRequest.getRepository().getName(),
                pullRequest.getPrNumber(),
                commitSha,
                githubComments,
                pullRequest.getRepository().getInstallationId()
            );

            // Post summary comment
            gitHubClientService.postSummaryComment(
                pullRequest.getRepository().getOwner(),
                pullRequest.getRepository().getName(),
                pullRequest.getPrNumber(),
                result.getSummary(),
                result.getComments().size(),
                pullRequest.getRepository().getInstallationId()
            );

            // Update review record
            long processingTime = System.currentTimeMillis() - startTime;
            review.setReviewStatus("completed");
            review.setTotalComments(comments.size());
            review.setTokensUsed(result.getTokensUsed());
            review.setProcessingTimeMs((int) processingTime);
            review.setSeverityCounts(calculateSeverityCounts(comments));
            reviewRepository.save(review);

            log.info("Review completed successfully. Comments: {}, Processing time: {}ms",
                comments.size(), processingTime);

        } catch (Exception e) {
            log.error("Error performing review for PR #{}", pullRequest.getPrNumber(), e);

            // Update review with error
            reviewRepository.findByPullRequestIdAndCommitSha(pullRequest.getId(), commitSha)
                .ifPresent(review -> {
                    review.setReviewStatus("failed");
                    review.setErrorMessage(e.getMessage());
                    reviewRepository.save(review);
                });
        }
    }

    private String detectLanguage(String diff) {
        // Simple language detection based on file extensions in diff
        if (diff.contains(".java")) return "Java";
        if (diff.contains(".py")) return "Python";
        if (diff.contains(".js") || diff.contains(".ts")) return "JavaScript/TypeScript";
        if (diff.contains(".go")) return "Go";
        if (diff.contains(".rb")) return "Ruby";
        return "Unknown";
    }

    private Map<String, Integer> calculateSeverityCounts(List<Comment> comments) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("info", 0);
        counts.put("warning", 0);
        counts.put("error", 0);

        for (Comment comment : comments) {
            counts.merge(comment.getSeverity(), 1, Integer::sum);
        }

        return counts;
    }
}
