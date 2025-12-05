package io.github._3xhaust.root_server.domain.garagesale.exception;

import io.github._3xhaust.root_server.global.common.exception.BaseException;

public class GarageSaleException extends BaseException {
    public GarageSaleException(GarageSaleErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}

