package com.codereview.assistant.service;

import com.codereview.assistant.dto.CodeReviewResult;
import com.codereview.assistant.dto.gitlab.GitLabMergeRequestEvent;
import com.codereview.assistant.exception.WebhookProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * GitLab Webhook 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabWebhookService {

    private final GitLabClientService gitLabClientService;
    private final CodeReviewService codeReviewService;

    /**
     * GitLab Merge Request 이벤트를 처리합니다
     *
     * @param event GitLab MR 이벤트
     */
    public void handleMergeRequestEvent(GitLabMergeRequestEvent event) {
        try {
            log.info("Processing GitLab MR event: {} - {} for project {}",
                    event.getObjectAttributes().getAction(),
                    event.getObjectAttributes().getTitle(),
                    event.getProject().getPathWithNamespace());

            // MR이 오픈되거나 업데이트될 때만 처리
            String action = event.getObjectAttributes().getAction();
            if (!"open".equals(action) && !"update".equals(action)) {
                log.info("Skipping MR action: {}", action);
                return;
            }

            // Work in Progress MR은 스킵
            if (Boolean.TRUE.equals(event.getObjectAttributes().getWorkInProgress())) {
                log.info("Skipping WIP merge request");
                return;
            }

            // 1. MR diff 가져오기
            Long projectId = event.getProject().getId();
            Long mergeRequestIid = event.getObjectAttributes().getIid();
            String diffContent = gitLabClientService.getMergeRequestDiff(projectId, mergeRequestIid);

            if (diffContent == null || diffContent.trim().isEmpty()) {
                log.warn("No diff content found for MR {}/{}", projectId, mergeRequestIid);
                return;
            }

            // 2. 언어 감지 (간단한 프로젝트 이름 기반 - 개선 필요)
            String language = detectLanguageFromProject(event.getProject().getName());

            // 3. AI 코드 리뷰 수행
            CodeReviewResult reviewResult = codeReviewService.analyzeCode(diffContent, language);

            // 4. GitLab에 코멘트 작성
            postReviewComments(projectId, mergeRequestIid, reviewResult,
                    event.getObjectAttributes().getLastCommit().getId());

            log.info("Successfully completed code review for GitLab MR {}/{}",
                    projectId, mergeRequestIid);

        } catch (Exception e) {
            log.error("Error processing GitLab MR event", e);
            throw new WebhookProcessingException("Failed to process GitLab MR webhook", e);
        }
    }

    /**
     * 리뷰 코멘트를 GitLab에 작성합니다
     */
    private void postReviewComments(Long projectId, Long mergeRequestIid,
                                     CodeReviewResult reviewResult, String commitSha) {
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

            gitLabClientService.postComment(projectId, mergeRequestIid, summaryBuilder.toString());

            // 2. 개별 라인 코멘트 작성
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

                // 라인 코멘트 작성 시도, 실패하면 일반 코멘트로 fallback
                if (comment.getLineNumber() != null) {
                    gitLabClientService.postLineComment(
                            projectId,
                            mergeRequestIid,
                            commitSha,
                            comment.getFilePath(),
                            comment.getLineNumber(),
                            commentBuilder.toString()
                    );
                } else {
                    // 라인 번호가 없으면 일반 코멘트로
                    gitLabClientService.postComment(projectId, mergeRequestIid,
                            String.format("**File: %s**\n\n%s", comment.getFilePath(), commentBuilder));
                }
            }

        } catch (Exception e) {
            log.error("Error posting review comments to GitLab", e);
            // 에러가 발생해도 전체 프로세스를 중단하지 않음
        }
    }

    /**
     * 프로젝트 정보에서 언어를 감지합니다 (간단한 휴리스틱)
     * TODO: 더 정교한 언어 감지 로직 구현 필요
     */
    private String detectLanguageFromProject(String projectName) {
        String lowerName = projectName.toLowerCase();

        if (lowerName.contains("java") || lowerName.contains("spring")) {
            return "Java";
        } else if (lowerName.contains("python") || lowerName.contains("py")) {
            return "Python";
        } else if (lowerName.contains("javascript") || lowerName.contains("js") ||
                lowerName.contains("react") || lowerName.contains("node")) {
            return "JavaScript";
        } else if (lowerName.contains("typescript") || lowerName.contains("ts")) {
            return "TypeScript";
        } else if (lowerName.contains("go") || lowerName.contains("golang")) {
            return "Go";
        } else if (lowerName.contains("rust")) {
            return "Rust";
        } else if (lowerName.contains("cpp") || lowerName.contains("c++")) {
            return "C++";
        }

        return "Unknown";
    }
}
