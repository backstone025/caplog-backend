package com.example.caplog.domain.users.dto.request;

public record UsersAlarmInfoRequest(
        boolean imminentAlarm,      // 임박한 알림
        boolean unviewedAlarm,      // 미열람 알림
        boolean aiRecommendedAlarm  // AI 추천 알림
) {
}
