package com.example.caplog.domain.notification.dto;

/**
 * notification 도메인에서 사용하는 page DTO
 *
 * @param totalPage  총 페이지 수
 * @param pageNumber 현재 페이지 번호
 */
public record NotificationPageInfo(
        Integer totalPage,
        Integer pageNumber
) {
}
