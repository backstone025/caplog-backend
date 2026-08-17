package com.example.caplog.domain.notification.controller;

import com.example.caplog.domain.notification.dto.NotificationGetAlarmListResponse;
import com.example.caplog.domain.notification.service.NotificationService;
import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
public class NotificationController {
    final NotificationService notificationService;

    // #9-1 이벤트 알림 목록 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<NotificationGetAlarmListResponse>> getAlarmList(
            @RequestParam Integer page,                 // 현재 조회하려는 페이지
            @RequestParam(defaultValue = "TOTAL") NotificationType alarmType    // 조회 알람 타입: default = TOTAL(전체 조회)
    ){
        NotificationGetAlarmListResponse response = notificationService.getAlarmList(page, alarmType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
