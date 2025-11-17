package com.codereview.assistant.service;

import com.codereview.assistant.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubClientService {

    @Value("${github.app.id}")
    private Long appId;

    @Value("${github.app.private-key}")
    private String privateKey;

    /**
     * Fetches the diff content for a pull request
     */
    public String getPullRequestDiff(String owner, String repo, int prNumber, Long installationId) {
        log.info("Fetching diff for PR: {}/{}#{}", owner, repo, prNumber);

        try {
            GitHub github = getGitHubClient(installationId);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            // Get diff using GitHub API
            return pullRequest.diff().toString();
        } catch (IOException e) {
            log.error("Failed to fetch diff for PR: {}/{}#{}", owner, repo, prNumber, e);
            throw new GitHubApiException(
                String.format("Failed to fetch diff for PR %s/%s#%d: %s", owner, repo, prNumber, e.getMessage()),
                e
            );
        }
    }

    /**
     * Posts review comments on a pull request
     */
    public void postReviewComments(
            String owner,
            String repo,
            int prNumber,
            String commitSha,
            List<ReviewCommentRequest> comments,
            Long installationId
    ) {
        log.info("Posting {} review comments for PR: {}/{}#{}",
            comments.size(), owner, repo, prNumber);

        try {
            GitHub github = getGitHubClient(installationId);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            // Create review with comments
            GHPullRequestReviewBuilder reviewBuilder = pullRequest.createReview()
                .event(GHPullRequestReviewEvent.COMMENT)
                .commitId(commitSha);

            for (ReviewCommentRequest comment : comments) {
                String body = formatCommentBody(comment);

                if (comment.getLineNumber() != null) {
                    reviewBuilder.comment(
                        body,
                        comment.getFilePath(),
                        comment.getLineNumber()
                    );
                }
            }

            reviewBuilder.create();
            log.info("Successfully posted review comments");
        } catch (IOException e) {
            log.error("Failed to post review comments for PR: {}/{}#{}", owner, repo, prNumber, e);
            throw new GitHubApiException(
                String.format("Failed to post review comments for PR %s/%s#%d: %s", owner, repo, prNumber, e.getMessage()),
                e
            );
        }
    }

    /**
     * Posts a summary comment on the pull request
     */
    public void postSummaryComment(
            String owner,
            String repo,
            int prNumber,
            String summary,
            int totalComments,
            Long installationId
    ) {
        log.info("Posting summary comment for PR: {}/{}#{}", owner, repo, prNumber);

        try {
            GitHub github = getGitHubClient(installationId);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            String commentBody = """
                ## 🤖 AI Code Review Summary

                %s

                **Total Issues Found:** %d

                ---
                *Powered by CodeReview AI Assistant*
                """.formatted(summary, totalComments);

            pullRequest.comment(commentBody);
            log.info("Successfully posted summary comment");
        } catch (IOException e) {
            log.error("Failed to post summary comment for PR: {}/{}#{}", owner, repo, prNumber, e);
            throw new GitHubApiException(
                String.format("Failed to post summary comment for PR %s/%s#%d: %s", owner, repo, prNumber, e.getMessage()),
                e
            );
        }
    }

    private GitHub getGitHubClient(Long installationId) throws IOException {
        // For GitHub App authentication
        // This is a simplified version - in production, you'd cache the token
        // and handle token refresh
        return new GitHubBuilder()
            .withAppInstallationToken(appId.toString(), privateKey, installationId)
            .build();
    }

    private String formatCommentBody(ReviewCommentRequest comment) {
        StringBuilder body = new StringBuilder();

        // Add severity emoji
        String emoji = switch (comment.getSeverity()) {
            case "error" -> "🔴";
            case "warning" -> "⚠️";
            default -> "ℹ️";
        };

        body.append(emoji).append(" **").append(comment.getCategory().toUpperCase()).append("**\n\n");
        body.append(comment.getMessage()).append("\n");

        if (comment.getSuggestion() != null && !comment.getSuggestion().isEmpty()) {
            body.append("\n**Suggestion:**\n").append(comment.getSuggestion()).append("\n");
        }

        if (comment.getCodeExample() != null && !comment.getCodeExample().isEmpty()) {
            body.append("\n**Example:**\n```\n")
                .append(comment.getCodeExample())
                .append("\n```\n");
        }

        return body.toString();
    }

    @lombok.Data
    @lombok.Builder
    public static class ReviewCommentRequest {
        private String filePath;
        private Integer lineNumber;
        private String severity;
        private String category;
        private String message;
        private String suggestion;
        private String codeExample;
    }
}
