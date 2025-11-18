package com.codereview.assistant.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 홈 컨트롤러
 *
 * 루트 경로와 대시보드 페이지 라우팅
 * GitHub 웹훅 URL이 루트로 설정된 경우 올바른 엔드포인트로 포워딩
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final WebhookController webhookController;

    @GetMapping("/")
    public String home() {
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    /**
     * GitHub 웹훅을 루트 URL에서 올바른 웹훅 엔드포인트로 포워딩합니다.
     * 웹훅 URL이 루트 도메인으로만 설정된 경우를 처리합니다.
     * (예: https://your-domain.com 대신 https://your-domain.com/api/webhook/github)
     */
    @PostMapping("/")
    public ResponseEntity<String> forwardWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload
    ) {
        log.info("Received POST request to root URL - forwarding to webhook handler");
        log.info("GitHub Event: {}, Has signature: {}", event, signature != null);

        // 실제 웹훅 핸들러로 포워딩
        return webhookController.handleGitHubWebhook(event, signature, payload);
    }
}
