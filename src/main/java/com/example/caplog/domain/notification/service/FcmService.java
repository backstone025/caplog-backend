package com.example.caplog.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    // 특정 사용자 FCM Token으로 푸시 알림 발송
    public void sendMessageTo(String targetToken, String title, String body) {
        if(targetToken == null || targetToken.isBlank()){
            log.warn("[FCM] 대상 FCM 토큰이 존재하지 않아 발송을 스킵합니다.");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 푸시 알림 발송 성공 - Message ID: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 푸시 알림 발송 실패 - Token: {}, Error: ", targetToken, e);
        }
    }
}
