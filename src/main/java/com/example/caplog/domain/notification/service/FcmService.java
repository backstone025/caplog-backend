package com.example.caplog.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class FcmService {
    private static final String CHANNEL_ID = "caplog_popup_channel";

    // 특정 사용자 FCM Token으로 푸시 알림 발송
    public void sendMessageTo(String targetToken, String title, String body,
                              String targetPage) {
        if(targetToken == null || targetToken.isBlank()){
            log.warn("[FCM] 대상 FCM 토큰이 존재하지 않아 발송을 스킵합니다.");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .setNotification(
                                            AndroidNotification.builder()
                                                    .setChannelId(CHANNEL_ID)
                                                    .build()
                                            )
                                            .build()
                    );

            if (targetPage != null && !targetPage.isBlank()) {
                messageBuilder.putData(
                        "targetPage",
                        targetPage
                );
            }
            Message message = messageBuilder.build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 푸시 알림 발송 성공 - Message ID: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 푸시 알림 발송 실패 - Token: {}, Error: ", targetToken, e);
        }
    }
}
