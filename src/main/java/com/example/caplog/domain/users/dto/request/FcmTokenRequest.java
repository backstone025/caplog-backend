package com.example.caplog.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank(message = "FCM 토큰은 필수 입력값입니다.")
        String fcmToken
) {}