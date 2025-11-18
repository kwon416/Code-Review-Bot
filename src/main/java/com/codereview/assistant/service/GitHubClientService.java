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

    @Value("${github.app.id:}")
    private String appId;

    @Value("${github.app.private-key:}")
    private String privateKey;

    @Value("${github.token:#{null}}")
    private String githubToken;

    /**
     * Fetches the diff content for a pull request
     */
    public String getPullRequestDiff(String owner, String repo, int prNumber, Long installationId) {
        log.info("Fetching diff for PR: {}/{}#{}", owner, repo, prNumber);

        try {
            GitHub github = getGitHubClient(installationId);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            // Get diff by listing files and creating a simple diff format
            StringBuilder diffBuilder = new StringBuilder();
            for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
                diffBuilder.append("diff --git a/").append(file.getFilename())
                    .append(" b/").append(file.getFilename()).append("\n");
                diffBuilder.append("--- a/").append(file.getFilename()).append("\n");
                diffBuilder.append("+++ b/").append(file.getFilename()).append("\n");
                diffBuilder.append("@@ -").append(file.getDeletions())
                    .append(" +").append(file.getAdditions()).append(" @@\n");
                diffBuilder.append(file.getPatch() != null ? file.getPatch() : "").append("\n\n");
            }

            return diffBuilder.toString();
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

        // Skip if no comments
        if (comments == null || comments.isEmpty()) {
            log.info("No review comments to post, skipping");
            return;
        }

        try {
            GitHub github = getGitHubClient(installationId);
            GHRepository repository = github.getRepository(owner + "/" + repo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            // Post individual comments instead of a review
            // (Personal Access Token doesn't support PR Review API)
            int successCount = 0;
            for (ReviewCommentRequest comment : comments) {
                try {
                    String body = formatCommentBody(comment);

                    if (comment.getLineNumber() != null && comment.getFilePath() != null) {
                        // Post as individual line comment
                        pullRequest.comment(body, commitSha, comment.getFilePath(), comment.getLineNumber());
                        successCount++;
                    } else {
                        // Post as general comment if no line number
                        pullRequest.comment(body);
                        successCount++;
                    }
                } catch (IOException e) {
                    log.warn("Failed to post individual comment: {}", e.getMessage());
                    // Continue with other comments
                }
            }

            log.info("Successfully posted {}/{} review comments", successCount, comments.size());
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
        // This is a simplified version - in production, you'd implement proper GitHub App auth

        // Use personal access token if available (for testing/development)
        if (githubToken != null && !githubToken.isEmpty()) {
            return new GitHubBuilder()
                .withOAuthToken(githubToken)
                .build();
        }

        // Otherwise, use anonymous access (limited rate)
        log.warn("No GitHub token configured - using anonymous access with limited rate");
        return new GitHubBuilder().build();
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
