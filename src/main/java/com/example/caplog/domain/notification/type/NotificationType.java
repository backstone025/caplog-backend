package com.example.caplog.domain.notification.type;

public enum NotificationType {
    TOTAL,          // (조회 용도로만 사용) 전 알림
    IMMINENT,       // 임박한 알림
    UNVIEWED,       // 미확인 알림
    AI_RECOMMENDED  // AI 추천 알림
}