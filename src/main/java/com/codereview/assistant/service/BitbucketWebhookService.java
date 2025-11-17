package com.codereview.assistant.service;

import com.codereview.assistant.dto.CodeReviewResult;
import com.codereview.assistant.dto.bitbucket.BitbucketPullRequestEvent;
import com.codereview.assistant.exception.WebhookProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bitbucket Webhook 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BitbucketWebhookService {

    private final BitbucketClientService bitbucketClientService;
    private final CodeReviewService codeReviewService;

    /**
     * Bitbucket Pull Request 이벤트를 처리합니다
     *
     * @param event Bitbucket PR 이벤트
     */
    public void handlePullRequestEvent(BitbucketPullRequestEvent event) {
        try {
            log.info("Processing Bitbucket PR event: {} for repository {}",
                    event.getPullRequest().getTitle(),
                    event.getRepository().getFullName());

            // PR 상태가 OPEN일 때만 처리
            if (!"OPEN".equals(event.getPullRequest().getState())) {
                log.info("Skipping non-open PR: {}", event.getPullRequest().getState());
                return;
            }

            // 1. PR diff 가져오기
            String fullName = event.getRepository().getFullName();
            String[] parts = fullName.split("/");
            if (parts.length != 2) {
                log.error("Invalid repository full name: {}", fullName);
                return;
            }

            String workspace = parts[0];
            String repoSlug = parts[1];
            Long prId = event.getPullRequest().getId();

            String diffContent = bitbucketClientService.getPullRequestDiff(workspace, repoSlug, prId);

            if (diffContent == null || diffContent.trim().isEmpty()) {
                log.warn("No diff content found for PR {}/{}/{}", workspace, repoSlug, prId);
                return;
            }

            // 2. 언어 감지
            String language = detectLanguage(event.getRepository().getLanguage());

            // 3. AI 코드 리뷰 수행
            CodeReviewResult reviewResult = codeReviewService.analyzeCode(diffContent, language);

            // 4. Bitbucket에 코멘트 작성
            postReviewComments(workspace, repoSlug, prId, reviewResult);

            log.info("Successfully completed code review for Bitbucket PR {}/{}/{}",
                    workspace, repoSlug, prId);

        } catch (Exception e) {
            log.error("Error processing Bitbucket PR event", e);
            throw new WebhookProcessingException("Failed to process Bitbucket PR webhook", e);
        }
    }

    /**
     * 리뷰 코멘트를 Bitbucket에 작성합니다
     */
    private void postReviewComments(String workspace, String repoSlug, Long prId,
                                     CodeReviewResult reviewResult) {
        try {
            // 1. 전체 요약 코멘트 작성
            StringBuilder summaryBuilder = new StringBuilder();
            summaryBuilder.append("## 🤖 AI Code Review Summary\n\n");
            summaryBuilder.append(reviewResult.getSummary()).append("\n\n");
            summaryBuilder.append(String.format("**Total Comments:** %d\n", reviewResult.getComments().size()));
            summaryBuilder.append(String.format("**Tokens Used:** %d\n", reviewResult.getTokensUsed()));

            // 심각도별 통계
            long errors = reviewResult.getComments().stream()
                    .filter(c -> "error".equals(c.getSeverity())).count();
            long warnings = reviewResult.getComments().stream()
                    .filter(c -> "warning".equals(c.getSeverity())).count();
            long infos = reviewResult.getComments().stream()
                    .filter(c -> "info".equals(c.getSeverity())).count();

            summaryBuilder.append(String.format("\n- 🔴 Errors: %d\n", errors));
            summaryBuilder.append(String.format("- 🟡 Warnings: %d\n", warnings));
            summaryBuilder.append(String.format("- 🔵 Info: %d\n", infos));

            bitbucketClientService.postComment(workspace, repoSlug, prId, summaryBuilder.toString());

            // 2. 개별 인라인 코멘트 작성
            for (CodeReviewResult.ReviewComment comment : reviewResult.getComments()) {
                StringBuilder commentBuilder = new StringBuilder();

                // 심각도 이모지
                String severityEmoji = switch (comment.getSeverity().toLowerCase()) {
                    case "error" -> "🔴";
                    case "warning" -> "🟡";
                    default -> "🔵";
                };

                commentBuilder.append(String.format("%s **%s - %s**\n\n",
                        severityEmoji,
                        comment.getSeverity().toUpperCase(),
                        comment.getCategory()));
                commentBuilder.append(comment.getMessage()).append("\n\n");

                if (comment.getSuggestion() != null && !comment.getSuggestion().isEmpty()) {
                    commentBuilder.append("**Suggestion:**\n");
                    commentBuilder.append(comment.getSuggestion()).append("\n\n");
                }

                if (comment.getCodeExample() != null && !comment.getCodeExample().isEmpty()) {
                    commentBuilder.append("**Example:**\n```\n");
                    commentBuilder.append(comment.getCodeExample()).append("\n```\n");
                }

                // 인라인 코멘트 작성 시도, 실패하면 일반 코멘트로 fallback
                if (comment.getLineNumber() != null) {
                    bitbucketClientService.postInlineComment(
                            workspace,
                            repoSlug,
                            prId,
                            comment.getFilePath(),
                            comment.getLineNumber(),
                            commentBuilder.toString()
                    );
                } else {
                    // 라인 번호가 없으면 일반 코멘트로
                    bitbucketClientService.postComment(workspace, repoSlug, prId,
                            String.format("**File: %s**\n\n%s", comment.getFilePath(), commentBuilder));
                }
            }

        } catch (Exception e) {
            log.error("Error posting review comments to Bitbucket", e);
            // 에러가 발생해도 전체 프로세스를 중단하지 않음
        }
    }

    /**
     * 레포지토리 언어 정보를 정규화합니다
     */
    private String detectLanguage(String repositoryLanguage) {
        if (repositoryLanguage == null || repositoryLanguage.isEmpty()) {
            return "Unknown";
        }

        String normalized = repositoryLanguage.toLowerCase();

        return switch (normalized) {
            case "java" -> "Java";
            case "python" -> "Python";
            case "javascript", "js" -> "JavaScript";
            case "typescript", "ts" -> "TypeScript";
            case "go", "golang" -> "Go";
            case "rust" -> "Rust";
            case "c++", "cpp" -> "C++";
            case "c#", "csharp" -> "C#";
            case "ruby" -> "Ruby";
            case "php" -> "PHP";
            case "swift" -> "Swift";
            case "kotlin" -> "Kotlin";
            default -> repositoryLanguage;
        };
    }
}
