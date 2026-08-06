package com.example.caplog.global.error.exception;

import com.example.caplog.global.error.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final Object result; // 에러 응답에 담을 상세 데이터

    // result 없이 에러 코드만 던질 때
    public GeneralException(BaseErrorCode errorCode) {
        this(errorCode, null);
    }

    // result와 함께 던질 때
    public GeneralException(BaseErrorCode errorCode, Object result) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.result = result;
    }
}
