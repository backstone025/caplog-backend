package com.example.caplog.domain.notification.entity;

import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long NotificationId;        // 알림 아이디

    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;                // 사용자

    @Enumerated(EnumType.STRING)
    private NotificationType type;      // 알림 타입

    private String title;               // 알림 제목

    private String content;             // 알림 내용

    private Boolean isRead;             // 알림 열람 여부
}
