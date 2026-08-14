package com.example.caplog.domain.auth.exception;

import com.example.caplog.global.error.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthException implements BaseErrorCode {
    LOGIN_ID_ALREADY_EXIST("AUTH_4001", "이미 존재하는 아이디 입니다", HttpStatus.BAD_REQUEST),
    LOGIN_ID_BAD_FORM("AUTH_4002", "로그인 아이디는 한/영 20자 이내야 합니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_BAD_FORM("AUTH_4003", "비밀번호는 숫자 4자 이어야 합니다.", HttpStatus.BAD_REQUEST)
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    AuthException(String code, String message, HttpStatus status) {
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
