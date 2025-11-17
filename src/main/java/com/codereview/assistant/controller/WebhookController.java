package com.codereview.assistant.controller;

import com.codereview.assistant.domain.PullRequest;
import com.codereview.assistant.dto.GitHubWebhookPayload;
import com.codereview.assistant.service.GitHubWebhookService;
import com.codereview.assistant.service.PullRequestService;
import com.codereview.assistant.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final GitHubWebhookService webhookService;
    private final PullRequestService pullRequestService;
    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload
    ) {
        log.info("Received GitHub webhook event: {}", event);

        try {
            // 1. Verify signature
            if (!webhookService.verifySignature(payload, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid signature");
            }

            // 2. Parse event
            GitHubWebhookPayload webhookPayload = objectMapper.readValue(
                payload, GitHubWebhookPayload.class
            );

            // 3. Handle pull request events
            if ("pull_request".equals(event)) {
                handlePullRequestEvent(webhookPayload);
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook: " + e.getMessage());
        }
    }

    private void handlePullRequestEvent(GitHubWebhookPayload payload) {
        String action = payload.getAction();
        log.info("Handling pull_request action: {}", action);

        // Trigger review on opened or synchronize (new commits)
        if ("opened".equals(action) || "synchronize".equals(action)) {
            PullRequest pullRequest = pullRequestService.handlePullRequestEvent(payload);

            String commitSha = payload.getPullRequest().getHead().getSha();

            // Queue review job (async)
            reviewService.performReview(pullRequest, commitSha);

            log.info("Review queued for PR #{}", pullRequest.getPrNumber());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
