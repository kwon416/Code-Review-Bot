package com.codereview.assistant.exception;

/**
 * 웹훅 이벤트 처리 중 발생하는 예외
 */
public class WebhookProcessingException extends RuntimeException {

    public WebhookProcessingException(String message) {
        super(message);
    }

    public WebhookProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
