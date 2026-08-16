package com.example.caplog.domain.users.dto.response;

public record UsersAlarmInfoResponse(
        boolean totalAlarm,         // 전체 알림
        boolean imminentAlarm,      // 임박한 알림
        boolean unviewedAlarm,      // 미열람 알림
        boolean aiRecommendedAlarm  // AI 추천 알림
) {
}
