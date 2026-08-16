package com.example.caplog.domain.notification.entity;

import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;        // 알림 아이디

    @JoinColumn(name = "users_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users users;                // 사용자

    @JoinColumn(name = "schedule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule;          // 일정 아이디

    @Enumerated(EnumType.STRING)
    private NotificationType type;      // 알림 타입

    private String title;               // 알림 제목

    private String content;             // 알림 내용

    private Boolean isRead;             // 알림 열람 여부

    @CreatedDate
    private LocalDateTime createdAt;    // 알림 생성 일시

    // 정적 팩토리 메소드
    public static Notification createNotification(
            Users users,
            Schedule schedule,
            NotificationType type,
            String title,
            String content,
            Boolean isRead) {
        Notification notification = new Notification();
        notification.users = users;
        notification.schedule = schedule;
        notification.type = type;
        notification.title = title;
        notification.content = content;
        notification.isRead = isRead;
        return notification;
    }

    // 알림 열람 처리
    public void markAsRead() {
        this.isRead = true;
    }
}
