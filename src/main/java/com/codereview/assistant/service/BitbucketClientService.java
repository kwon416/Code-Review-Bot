package com.codereview.assistant.service;

import com.codereview.assistant.exception.BitbucketApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;

/**
 * Bitbucket API 클라이언트 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BitbucketClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${bitbucket.api.url:https://api.bitbucket.org/2.0}")
    private String bitbucketApiUrl;

    @Value("${bitbucket.username:}")
    private String bitbucketUsername;

    @Value("${bitbucket.app.password:}")
    private String bitbucketAppPassword;

    /**
     * Pull Request의 diff 내용을 가져옵니다
     *
     * @param workspace 워크스페이스 이름
     * @param repoSlug 레포지토리 slug
     * @param prId Pull Request ID
     * @return diff 내용
     */
    public String getPullRequestDiff(String workspace, String repoSlug, Long prId) {
        try {
            String url = String.format("%s/repositories/%s/%s/pullrequests/%d/diff",
                    bitbucketApiUrl, workspace, repoSlug, prId);

            HttpHeaders headers = createHeaders();
            headers.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new BitbucketApiException("Failed to fetch PR diff: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error fetching Bitbucket PR diff for {}/{} PR {}",
                    workspace, repoSlug, prId, e);
            throw new BitbucketApiException("Failed to fetch Bitbucket PR diff", e);
        }
    }

    /**
     * Pull Request에 코멘트를 작성합니다
     *
     * @param workspace 워크스페이스 이름
     * @param repoSlug 레포지토리 slug
     * @param prId Pull Request ID
     * @param content 코멘트 내용
     */
    public void postComment(String workspace, String repoSlug, Long prId, String content) {
        try {
            String url = String.format("%s/repositories/%s/%s/pullrequests/%d/comments",
                    bitbucketApiUrl, workspace, repoSlug, prId);

            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var requestBody = objectMapper.createObjectNode();
            var contentNode = requestBody.putObject("content");
            contentNode.put("raw", content);

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
                log.info("Successfully posted comment to Bitbucket PR {}/{}/{}",
                        workspace, repoSlug, prId);
            } else {
                throw new BitbucketApiException("Failed to post comment: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error posting comment to Bitbucket PR {}/{}/{}",
                    workspace, repoSlug, prId, e);
            throw new BitbucketApiException("Failed to post Bitbucket comment", e);
        }
    }

    /**
     * Pull Request에 인라인 코멘트를 작성합니다
     *
     * @param workspace 워크스페이스 이름
     * @param repoSlug 레포지토리 slug
     * @param prId Pull Request ID
     * @param filePath 파일 경로
     * @param lineNumber 라인 번호
     * @param content 코멘트 내용
     */
    public void postInlineComment(String workspace, String repoSlug, Long prId,
                                   String filePath, Integer lineNumber, String content) {
        try {
            String url = String.format("%s/repositories/%s/%s/pullrequests/%d/comments",
                    bitbucketApiUrl, workspace, repoSlug, prId);

            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var requestBody = objectMapper.createObjectNode();

            // Content
            var contentNode = requestBody.putObject("content");
            contentNode.put("raw", content);

            // Inline location
            var inlineNode = requestBody.putObject("inline");
            inlineNode.put("to", lineNumber);
            inlineNode.put("path", filePath);

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
                log.info("Successfully posted inline comment to Bitbucket PR {}/{}/{} at {}:{}",
                        workspace, repoSlug, prId, filePath, lineNumber);
            } else {
                log.warn("Failed to post inline comment: {}", response.getStatusCode());
                // Fallback to regular comment
                postComment(workspace, repoSlug, prId,
                        String.format("**%s:%d**\n\n%s", filePath, lineNumber, content));
            }

        } catch (Exception e) {
            log.error("Error posting inline comment to Bitbucket PR {}/{}/{}",
                    workspace, repoSlug, prId, e);
            // Fallback to regular comment
            try {
                postComment(workspace, repoSlug, prId,
                        String.format("**%s:%d**\n\n%s", filePath, lineNumber, content));
            } catch (Exception ex) {
                log.error("Failed to post fallback comment", ex);
            }
        }
    }

    /**
     * HTTP 헤더를 생성합니다 (Basic Auth 포함)
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();

        if (bitbucketUsername != null && !bitbucketUsername.isEmpty() &&
            bitbucketAppPassword != null && !bitbucketAppPassword.isEmpty()) {

            String auth = bitbucketUsername + ":" + bitbucketAppPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        return headers;
    }
}
