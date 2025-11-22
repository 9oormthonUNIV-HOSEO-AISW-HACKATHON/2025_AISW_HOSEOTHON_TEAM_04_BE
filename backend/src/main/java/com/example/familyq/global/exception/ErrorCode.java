package com.example.familyq.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다."),
    LOGIN_ID_DUPLICATED(HttpStatus.CONFLICT, "USER_409_01", "이미 사용 중인 아이디입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER_401_01", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "USER_403_01", "관리자만 접근할 수 있습니다."),

    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_404", "가족 정보를 찾을 수 없습니다."),
    FAMILY_CODE_DUPLICATED(HttpStatus.CONFLICT, "FAMILY_409_01", "이미 사용 중인 가족 코드입니다."),
    FAMILY_CODE_INVALID(HttpStatus.BAD_REQUEST, "FAMILY_400_01", "유효하지 않은 가족 코드입니다."),
    USER_ALREADY_HAS_FAMILY(HttpStatus.CONFLICT, "FAMILY_409_02", "이미 가족에 속해 있습니다."),
    USER_NOT_IN_FAMILY(HttpStatus.BAD_REQUEST, "FAMILY_400_02", "가족에 속해 있지 않습니다."),
    FAMILY_NOT_READY_FOR_QUESTIONS(HttpStatus.BAD_REQUEST, "FAMILY_400_03", "가족이 아직 질문을 시작할 준비가 되지 않았습니다."),

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_404", "질문 정보를 찾을 수 없습니다."),
    QUESTION_IN_USE(HttpStatus.CONFLICT, "QUESTION_409_01", "이미 사용 중인 질문은 삭제할 수 없습니다."),
    FAMILY_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_QUESTION_404", "가족 질문을 찾을 수 없습니다."),
    FAMILY_QUESTION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "FAMILY_QUESTION_409", "이미 완료된 질문입니다."),

    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "ANSWER_404", "답변을 찾을 수 없습니다."),

    AI_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500", "인사이트 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
