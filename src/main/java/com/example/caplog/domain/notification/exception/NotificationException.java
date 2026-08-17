package com.example.caplog.domain.notification.exception;

import com.example.caplog.global.error.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum NotificationException implements BaseErrorCode {
    NOTIFICATION_PAGE_BAD_RANGE("NOTIFICATION_4001", "입력한 페이지는 범위 내 유효하지 않습니다.",  HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    NotificationException(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }
}
