package com.example.caplog.domain.notification.controller;

import com.example.caplog.domain.notification.dto.request.NotificationAlarmConsentRequest;
import com.example.caplog.domain.notification.dto.response.NotificationAlarmConsentResponse;
import com.example.caplog.domain.notification.dto.response.NotificationGetAlarmListResponse;
import com.example.caplog.domain.notification.service.NotificationService;
import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ) {
        NotificationGetAlarmListResponse response = notificationService.getAlarmList(page, alarmType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #9-2 사용자 알림 동의 확정
    @PutMapping("/alarm-consent")
    public ResponseEntity<ApiResponse<NotificationAlarmConsentResponse>> putPhotoConsent(
            @RequestBody NotificationAlarmConsentRequest request
    ) {
        NotificationAlarmConsentResponse response = notificationService.updateAlarmConsent(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #9-3 사용자 알림 동의여부 조회
    @GetMapping("/alarm-consent")
    public ResponseEntity<ApiResponse<NotificationAlarmConsentResponse>> getAlarmConsent(){
        NotificationAlarmConsentResponse response = notificationService.getAlarmConsent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
