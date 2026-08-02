package com.example.caplog.global.response;

import com.example.caplog.global.error.code.BaseErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, "COMMON_200", "요청에 성공했습니다.", result);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> fail(BaseErrorCode errorCode, T result) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), result);
    }

    public static ApiResponse<Void> fail(BaseErrorCode errorCode) {
        return fail(errorCode, null);
    }
}

