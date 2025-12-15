package io.github._3xhaust.root_server.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_ARGUMENT("INVALID_ARGUMENT", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    BAD_CREDENTIALS("BAD_CREDENTIALS", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
