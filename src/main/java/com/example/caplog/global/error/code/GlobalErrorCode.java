package com.example.caplog.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {
    INVALID_INPUT_VALUE("COMMON_400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("COMMON_500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    S3_UPLOAD_FAILED("S3_500_1", "이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    S3_DELETE_FAILED("S3_500_2", "이미지 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NOT_FOUND("IMAGE_404", "이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND("SCHEDULE_404", "존재하지 않는 일정입니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_INVALID_DETAILS("SCHEDULE_4003", "입력하신 상세 정보에 유효하지 않은 값이 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND("GROUP_4041", "존재하지 않는 그룹입니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_INVALID_PAGE("SCHEDULE_4001", "입력한 페이지는 범위 내 유효하지 않습니다.", HttpStatus.BAD_REQUEST);


    private final String code;
    private final String message;
    private final HttpStatus status;
}
