package com.codereview.assistant.controller;

import com.codereview.assistant.dto.gitlab.GitLabMergeRequestEvent;
import com.codereview.assistant.service.GitLabWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GitLab Webhook 컨트롤러
 */
@RestController
@RequestMapping("/api/webhook/gitlab")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GitLab Webhook", description = "GitLab 웹훅 API")
public class GitLabWebhookController {

    private final GitLabWebhookService gitLabWebhookService;

    /**
     * GitLab Merge Request 웹훅 처리
     */
    @PostMapping
    @Operation(summary = "GitLab 웹훅 수신", description = "GitLab Merge Request 이벤트를 처리합니다")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestHeader(value = "X-Gitlab-Event", required = false) String gitlabEvent,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String gitlabToken,
            @RequestBody GitLabMergeRequestEvent event) {

        log.info("Received GitLab webhook event: {}", gitlabEvent);

        try {
            // Merge Request 이벤트만 처리
            if ("Merge Request Hook".equals(gitlabEvent) ||
                "merge_request".equals(event.getObjectKind())) {

                // 비동기로 처리 (웹훅 응답 시간 단축)
                new Thread(() -> {
                    try {
                        gitLabWebhookService.handleMergeRequestEvent(event);
                    } catch (Exception e) {
                        log.error("Error handling GitLab MR event", e);
                    }
                }).start();

                return ResponseEntity.ok(Map.of(
                        "status", "accepted",
                        "message", "Merge request event queued for processing"
                ));
            } else {
                log.info("Ignoring non-MR GitLab event: {}", gitlabEvent);
                return ResponseEntity.ok(Map.of(
                        "status", "ignored",
                        "message", "Event type not supported"
                ));
            }

        } catch (Exception e) {
            log.error("Error processing GitLab webhook", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Health check 엔드포인트
     */
    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "GitLab 웹훅 엔드포인트 상태 확인")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "gitlab-webhook"
        ));
    }
}
