package com.codereview.assistant.controller;

import com.codereview.assistant.dto.bitbucket.BitbucketPullRequestEvent;
import com.codereview.assistant.service.BitbucketWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Bitbucket Webhook 컨트롤러
 */
@RestController
@RequestMapping("/api/webhook/bitbucket")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bitbucket Webhook", description = "Bitbucket 웹훅 API")
public class BitbucketWebhookController {

    private final BitbucketWebhookService bitbucketWebhookService;

    /**
     * Bitbucket Pull Request 웹훅 처리
     */
    @PostMapping
    @Operation(summary = "Bitbucket 웹훅 수신", description = "Bitbucket Pull Request 이벤트를 처리합니다")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestHeader(value = "X-Event-Key", required = false) String eventKey,
            @RequestHeader(value = "X-Request-UUID", required = false) String requestId,
            @RequestBody BitbucketPullRequestEvent event) {

        log.info("Received Bitbucket webhook event: {} (request: {})", eventKey, requestId);

        try {
            // Pull Request 이벤트만 처리
            if (eventKey != null && eventKey.startsWith("pullrequest:")) {

                // 비동기로 처리 (웹훅 응답 시간 단축)
                new Thread(() -> {
                    try {
                        bitbucketWebhookService.handlePullRequestEvent(event);
                    } catch (Exception e) {
                        log.error("Error handling Bitbucket PR event", e);
                    }
                }).start();

                return ResponseEntity.ok(Map.of(
                        "status", "accepted",
                        "message", "Pull request event queued for processing"
                ));
            } else {
                log.info("Ignoring non-PR Bitbucket event: {}", eventKey);
                return ResponseEntity.ok(Map.of(
                        "status", "ignored",
                        "message", "Event type not supported"
                ));
            }

        } catch (Exception e) {
            log.error("Error processing Bitbucket webhook", e);
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
    @Operation(summary = "헬스 체크", description = "Bitbucket 웹훅 엔드포인트 상태 확인")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "bitbucket-webhook"
        ));
    }
}
