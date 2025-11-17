package com.codereview.assistant.exception;

/**
 * Bitbucket API 호출 중 발생하는 예외
 */
public class BitbucketApiException extends RuntimeException {

    public BitbucketApiException(String message) {
        super(message);
    }

    public BitbucketApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
