package io.github._3xhaust.root_server.domain.garagesale.exception;

import io.github._3xhaust.root_server.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GarageSaleErrorCode implements ErrorCode {
    GARAGE_SALE_NOT_FOUND("GARAGE_SALE_NOT_FOUND", HttpStatus.NOT_FOUND, "개러지 세일을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", HttpStatus.FORBIDDEN, "해당 개러지 세일에 대한 권한이 없습니다."),
    ALREADY_FAVORITED("ALREADY_FAVORITED", HttpStatus.CONFLICT, "이미 즐겨찾기로 등록되어 있습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}

