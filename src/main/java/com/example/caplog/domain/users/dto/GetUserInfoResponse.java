package com.example.caplog.domain.users.dto;

import com.example.caplog.domain.users.type.ProfileImage;

public record GetUserInfoResponse(
        String userName,            // 사용자 이름
        String imgUrl,              // 프로필 이미지 URL 주소
        Integer totalSchedule,      // 전체 일정 개수
        Integer thisMonthSchedule   // 이번 달 등록 일정 개수
) {
}
