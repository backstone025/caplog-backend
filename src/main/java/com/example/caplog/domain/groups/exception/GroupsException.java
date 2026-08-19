package com.example.caplog.domain.groups.exception;

import com.example.caplog.global.error.code.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum GroupsException implements BaseErrorCode {
    GROUP_NOT_FOUND("GROUP_404", "해당 그룹은 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    GROUP_PAGE_BAD_RANGE("GROUP_4001", "입력한 페이지는 범위 내 유효하지 않습니다.",  HttpStatus.BAD_REQUEST),
    GROUP_NAME_BAD_FORM("GROUP_4002", "입력하신 그룹 이름은 잘못된 값입니다.", HttpStatus.BAD_REQUEST),
    GROUP_NAME_ALREADY_EXIST("GROUP_4003", "입력하신 그룹 이름은 이미 존재하는 값입니다.", HttpStatus.BAD_REQUEST),
    GROUP_CATEGORY_BAD_FORM("GROUP_4004", "입력하신 그룹 카테고리는 잘못된 값입니다.",  HttpStatus.BAD_REQUEST),
    GROUP_USER_NOT_LOGIN_USER("GROUP_403", "로그인 사용자와 그룹의 사용자가 일치하지 않습니다.", HttpStatus.FORBIDDEN),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    GroupsException(String code, String message, HttpStatus status) {
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
