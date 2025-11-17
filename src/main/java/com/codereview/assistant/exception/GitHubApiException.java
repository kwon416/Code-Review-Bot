package com.codereview.assistant.exception;

/**
 * GitHub API 호출 중 발생하는 예외
 */
public class GitHubApiException extends RuntimeException {

    public GitHubApiException(String message) {
        super(message);
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
