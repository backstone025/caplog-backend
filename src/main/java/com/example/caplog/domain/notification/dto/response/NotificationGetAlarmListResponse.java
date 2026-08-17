package com.example.caplog.domain.notification.dto.response;

import com.example.caplog.domain.notification.dto.NotificationPageInfo;
import com.example.caplog.domain.notification.entity.Notification;
import com.example.caplog.domain.notification.type.NotificationType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record NotificationGetAlarmListResponse(
        NotificationPageInfo page,                  // 페이지 정보
        NotificationType alarmType,                 // 조회 알림 타입
        Long alarmCount,                            // 총 알림 개수
        List<NotificationElement> notifications     // 알림 리스트 정보
) {
    private record NotificationElement(
            Long alarmId,                           // 알림 ID
            Long scheduleId,                        // 연관 일정 ID
            String title,                           // 알림 제목
            Long Dday,                              // 남일 날
            NotificationType alarmType,             // 알림 타입
            String imgUrl,                          // 대표 썸내일 URL
            String message                          // 알림 메세지
    ) {
    }

    public static NotificationGetAlarmListResponse from(
            Page<Notification> notificationPage,
            NotificationType alarmType,
            Map<Long, Long> dDayMapper,
            Map<Long, String> imagesMapper
    ) {
        // pageInfo
        NotificationPageInfo pageInfo = new NotificationPageInfo(
                notificationPage.getTotalPages(),
                notificationPage.getNumber()
        );

        // alarmCount
        Long alarmCount = notificationPage.getTotalElements();

        // notifications
        List<NotificationElement> notifications = notificationPage.stream()
                .map(n -> new NotificationElement(
                        n.getNotificationId(),
                        n.getSchedule().getScheduleId(),
                        n.getTitle(),
                        dDayMapper.get(n.getNotificationId()),
                        n.getType(),
                        imagesMapper.get(n.getNotificationId()),
                        n.getContent()
                ))
                .toList();
        return new NotificationGetAlarmListResponse(pageInfo, alarmType, alarmCount, notifications);
    }
}
