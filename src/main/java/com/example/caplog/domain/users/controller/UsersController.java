package com.example.caplog.domain.users.controller;

import com.example.caplog.domain.users.dto.request.FcmTokenRequest;
import com.example.caplog.domain.users.dto.request.UsersAlarmInfoRequest;
import com.example.caplog.domain.users.dto.request.UsersPhotoConsentRequest;
import com.example.caplog.domain.users.dto.request.UsersProfileInfoRequest;
import com.example.caplog.domain.users.dto.response.*;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    // #1-1 로그인한 사용자 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<GetUserInfoResponse>> getUserInfo() {
        GetUserInfoResponse response = usersService.getUserInfo();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-2 사용자 사진 접근 동의 확정
    @PutMapping("/photo-consent")
    public ResponseEntity<ApiResponse<UsersPhotoConsentResponse>> putPhotoConsent(
            @RequestBody UsersPhotoConsentRequest request
    ) {
        UsersPhotoConsentResponse response = usersService.putUsersPhotoConsent(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-3 사용자 사진 접근 동의 확정
    @GetMapping("/photo-consent")
    public ResponseEntity<ApiResponse<UsersPhotoConsentResponse>> getPhotoConsent() {
        UsersPhotoConsentResponse response = usersService.getUsersPhotoConsent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-4-1 사용자 프로필 정보 조회
    @GetMapping("/settings/profile")
    public ResponseEntity<ApiResponse<UsersProfileInfoResponse>> getUserProfileInfo() {
        UsersProfileInfoResponse response = usersService.getUserProfileInfo();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-4-2 사용자 프로필 수정
    @PostMapping("/settings/profile")
    public ResponseEntity<ApiResponse<UsersProfileInfoResponse>> updateUserProfileInfo(
            @RequestBody UsersProfileInfoRequest request
    ) {
        UsersProfileInfoResponse response = usersService.updateUserProfileInfo(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-5-1 사용자 알림 설정 정보 조회
    @GetMapping("/settings/alarms")
    public ResponseEntity<ApiResponse<UsersAlarmInfoResponse>> getUserAlarmsInfo() {
        UsersAlarmInfoResponse response = usersService.getUsersAlarmConsent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-5-2 사용자 알림 설정
    @PostMapping("/settings/alarms")
    public ResponseEntity<ApiResponse<UsersAlarmInfoResponse>> updateUserAlarmsInfo(
            @RequestBody UsersAlarmInfoRequest request
    ) {
        UsersAlarmInfoResponse response = usersService.updateUsersAlarmConsent(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-6 프로필 사진 URL 조회
    @GetMapping("/profile-img")
    public ResponseEntity<ApiResponse<UsersProfileImgUrlListResponse>> getUserProfileImgUrlList() {
        UsersProfileImgUrlListResponse response = usersService.getUserProfileImgUrlList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #1-7 FCM 토큰 갱신(앱 전용)
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        usersService.updateFcmToken(request.fcmToken());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
