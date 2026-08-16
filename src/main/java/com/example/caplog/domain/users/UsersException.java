package com.example.caplog.domain.users;

import com.example.caplog.global.error.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UsersException implements BaseErrorCode {
    USERS_PROFILE_IMAGE_BAD_REQUEST("USERS_4001", "이미지 타입은 BLUE, GREEN, ORANGE 중에서만 택할 수 있습니다.", HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    UsersException(String code, String message, HttpStatus status) {
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
