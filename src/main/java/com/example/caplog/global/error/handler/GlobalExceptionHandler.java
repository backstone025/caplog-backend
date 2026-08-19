package com.example.caplog.global.error.handler;

import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import com.example.caplog.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생한 예외를 공통 API 응답 형식으로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 의도적으로 발생시킨 커스텀 예외를 처리한다.
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(GeneralException e) {
        // result가 있으면 데이터 포함, 없으면 null(ApiResponse에서 NON_NULL 옵션으로 자동 제거됨)
        ApiResponse<Object> response = ApiResponse.fail(e.getErrorCode(), e.getResult());

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }

    /**
     * @Valid 검증 실패 시 첫 번째 필드 오류 메시지를 내려준다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        log.error("[Validation Exception 발생]", exception); // ★ 로깅 추가

        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError == null ? GlobalErrorCode.INVALID_INPUT_VALUE.getMessage() : fieldError.getDefaultMessage();

        return ResponseEntity
                .status(GlobalErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(new ApiResponse<>(false, GlobalErrorCode.INVALID_INPUT_VALUE.getCode(), message, null));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalAuthenticationException(InternalAuthenticationServiceException exception) {
        log.error("[Authentication Exception 발생]", exception); // ★ 로깅 추가

        // 내부에 진짜 원인이 된 예외가 GeneralException인지 확인
        if (exception.getCause() instanceof GeneralException generalException) {
            return ResponseEntity
                    .status(generalException.getErrorCode().getStatus())
                    .body(ApiResponse.fail(generalException.getErrorCode()));
        }

        // 만약 다른 시큐리티 에러라면 기본 500 에러 처리
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 처리되지 않은 예외를 최종적으로 받아 내부 서버 오류 응답으로 변환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("[Unhandled Exception 발생 - 서버 500/400 추적 로그]", exception); // ★ 가장 중요한 추적 로그 추가

        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }
}
