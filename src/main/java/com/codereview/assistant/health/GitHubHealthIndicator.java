package com.codereview.assistant.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * GitHub API 헬스 체크
 *
 * GitHub API 연결 상태 및 Rate Limit 정보를 확인합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubHealthIndicator implements HealthIndicator {

    @Value("${github.app.id:}")
    private String appId;

    @Value("${github.app.private-key:}")
    private String privateKey;

    @Override
    public Health health() {
        try {
            // 기본적인 GitHub 연결 확인
            if (appId == null || appId.isEmpty() || privateKey == null || privateKey.isEmpty()) {
                return Health.down()
                    .withDetail("reason", "GitHub App credentials not configured")
                    .build();
            }

            // GitHub API 기본 연결 테스트
            GitHub github = new GitHubBuilder().build();

            // Rate limit 정보 가져오기
            var rateLimit = github.getRateLimit();
            int remaining = rateLimit.getRemaining();
            int limit = rateLimit.getLimit();

            if (remaining < limit * 0.1) { // 10% 미만 남은 경우 경고
                return Health.up()
                    .withDetail("status", "warning")
                    .withDetail("message", "GitHub API rate limit running low")
                    .withDetail("remaining", remaining)
                    .withDetail("limit", limit)
                    .build();
            }

            return Health.up()
                .withDetail("status", "healthy")
                .withDetail("remaining_requests", remaining)
                .withDetail("total_limit", limit)
                .build();

        } catch (Exception e) {
            log.error("GitHub health check failed", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
