package io.github._3xhaust.root_server.domain.community.exception;

import io.github._3xhaust.root_server.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommunityErrorCode implements ErrorCode {
    COMMUNITY_NOT_FOUND("COMMUNITY_NOT_FOUND", HttpStatus.NOT_FOUND, "커뮤니티를 찾을 수 없습니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
