package com.codereview.assistant.service;

import com.codereview.assistant.exception.GitLabApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * GitLab API 클라이언트 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${gitlab.api.url:https://gitlab.com/api/v4}")
    private String gitlabApiUrl;

    @Value("${gitlab.token:}")
    private String gitlabToken;

    /**
     * Merge Request의 diff 내용을 가져옵니다
     *
     * @param projectId 프로젝트 ID
     * @param mergeRequestIid Merge Request IID
     * @return diff 내용
     */
    public String getMergeRequestDiff(Long projectId, Long mergeRequestIid) {
        try {
            String url = String.format("%s/projects/%d/merge_requests/%d/changes",
                    gitlabApiUrl, projectId, mergeRequestIid);

            HttpHeaders headers = new HttpHeaders();
            if (gitlabToken != null && !gitlabToken.isEmpty()) {
                headers.set("PRIVATE-TOKEN", gitlabToken);
            }
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseDiffFromChanges(response.getBody());
            } else {
                throw new GitLabApiException("Failed to fetch MR changes: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error fetching GitLab MR diff for project {} MR {}", projectId, mergeRequestIid, e);
            throw new GitLabApiException("Failed to fetch GitLab MR diff", e);
        }
    }

    /**
     * GitLab changes API 응답에서 diff를 파싱합니다
     */
    private String parseDiffFromChanges(String changesJson) {
        try {
            JsonNode root = objectMapper.readTree(changesJson);
            JsonNode changes = root.get("changes");

            if (changes == null || !changes.isArray()) {
                return "";
            }

            StringBuilder diffBuilder = new StringBuilder();

            for (JsonNode change : changes) {
                String oldPath = change.has("old_path") ? change.get("old_path").asText() : "";
                String newPath = change.has("new_path") ? change.get("new_path").asText() : "";
                String diff = change.has("diff") ? change.get("diff").asText() : "";

                // Git diff 형식으로 변환
                diffBuilder.append("diff --git a/").append(oldPath)
                        .append(" b/").append(newPath).append("\n");

                if (change.has("new_file") && change.get("new_file").asBoolean()) {
                    diffBuilder.append("new file mode 100644\n");
                } else if (change.has("deleted_file") && change.get("deleted_file").asBoolean()) {
                    diffBuilder.append("deleted file mode 100644\n");
                } else if (change.has("renamed_file") && change.get("renamed_file").asBoolean()) {
                    diffBuilder.append("rename from ").append(oldPath).append("\n");
                    diffBuilder.append("rename to ").append(newPath).append("\n");
                }

                diffBuilder.append(diff).append("\n");
            }

            return diffBuilder.toString();

        } catch (Exception e) {
            log.error("Error parsing GitLab changes", e);
            throw new GitLabApiException("Failed to parse GitLab changes", e);
        }
    }

    /**
     * Merge Request에 코멘트를 작성합니다
     *
     * @param projectId 프로젝트 ID
     * @param mergeRequestIid Merge Request IID
     * @param body 코멘트 내용
     */
    public void postComment(Long projectId, Long mergeRequestIid, String body) {
        try {
            String url = String.format("%s/projects/%d/merge_requests/%d/notes",
                    gitlabApiUrl, projectId, mergeRequestIid);

            HttpHeaders headers = new HttpHeaders();
            if (gitlabToken != null && !gitlabToken.isEmpty()) {
                headers.set("PRIVATE-TOKEN", gitlabToken);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(
                    Collections.singletonMap("body", body)
            );

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully posted comment to GitLab MR {}/{}", projectId, mergeRequestIid);
            } else {
                throw new GitLabApiException("Failed to post comment: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error posting comment to GitLab MR {}/{}", projectId, mergeRequestIid, e);
            throw new GitLabApiException("Failed to post GitLab comment", e);
        }
    }

    /**
     * Merge Request에 라인 코멘트를 작성합니다
     *
     * @param projectId 프로젝트 ID
     * @param mergeRequestIid Merge Request IID
     * @param commitSha 커밋 SHA
     * @param filePath 파일 경로
     * @param lineNumber 라인 번호
     * @param body 코멘트 내용
     */
    public void postLineComment(Long projectId, Long mergeRequestIid, String commitSha,
                                 String filePath, Integer lineNumber, String body) {
        try {
            String url = String.format("%s/projects/%d/merge_requests/%d/discussions",
                    gitlabApiUrl, projectId, mergeRequestIid);

            HttpHeaders headers = new HttpHeaders();
            if (gitlabToken != null && !gitlabToken.isEmpty()) {
                headers.set("PRIVATE-TOKEN", gitlabToken);
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            // GitLab discussion API 요청 본문
            var requestBody = objectMapper.createObjectNode();
            requestBody.put("body", body);

            var position = requestBody.putObject("position");
            position.put("position_type", "text");
            position.put("base_sha", commitSha);
            position.put("head_sha", commitSha);
            position.put("start_sha", commitSha);
            position.put("new_path", filePath);
            position.put("new_line", lineNumber);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully posted line comment to GitLab MR {}/{} at {}:{}",
                        projectId, mergeRequestIid, filePath, lineNumber);
            } else {
                log.warn("Failed to post line comment: {}", response.getStatusCode());
                // Fallback to regular comment
                postComment(projectId, mergeRequestIid,
                        String.format("**%s:%d**\n\n%s", filePath, lineNumber, body));
            }

        } catch (Exception e) {
            log.error("Error posting line comment to GitLab MR {}/{}", projectId, mergeRequestIid, e);
            // Fallback to regular comment
            try {
                postComment(projectId, mergeRequestIid,
                        String.format("**%s:%d**\n\n%s", filePath, lineNumber, body));
            } catch (Exception ex) {
                log.error("Failed to post fallback comment", ex);
            }
        }
    }
}
