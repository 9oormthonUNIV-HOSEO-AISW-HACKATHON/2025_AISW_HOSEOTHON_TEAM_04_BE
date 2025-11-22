package com.example.familyq.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiErrorResponse {

    private final String timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;

    public static ApiErrorResponse from(ErrorCode errorCode, String path) {
        return ApiErrorResponse.builder()
                .timestamp(String.valueOf(LocalDateTime.now()))
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String customMessage, String path) {
        return ApiErrorResponse.builder()
                .timestamp(String.valueOf(LocalDateTime.now()))
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(customMessage)
                .path(path)
                .build();
    }
}
