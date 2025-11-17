package com.codereview.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 표준 에러 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 에러 코드 (HTTP 상태 코드)
     */
    private int status;

    /**
     * 에러 메시지
     */
    private String message;

    /**
     * 에러 상세 설명
     */
    private String details;

    /**
     * 에러가 발생한 경로
     */
    private String path;

    /**
     * 에러 발생 시각
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 유효성 검증 실패 시 필드별 에러 목록
     */
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        /**
         * 필드명
         */
        private String field;

        /**
         * 거부된 값
         */
        private Object rejectedValue;

        /**
         * 에러 메시지
         */
        private String message;
    }
}
