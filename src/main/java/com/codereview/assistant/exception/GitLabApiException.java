package com.codereview.assistant.exception;

/**
 * GitLab API 호출 중 발생하는 예외
 */
public class GitLabApiException extends RuntimeException {

    public GitLabApiException(String message) {
        super(message);
    }

    public GitLabApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
