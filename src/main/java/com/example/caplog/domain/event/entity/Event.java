package com.example.caplog.domain.event.entity;

import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;               // 이벤트 아이디

    @JoinColumn(name = "schedule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)      // DB의 'On Delete Cascade' 기능 활성화
    private Schedule schedule;          // 일정 아이디

    @JoinColumn(name = "image_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Images images;              // 이미지 아이디

    private String title;               // 이벤트 제목

    private String details;             // 세부사항

    private String aiSummary;           // AI 요약

    private String videoUrl;            // 영상 링크

    private LocalDateTime startAt;      // 시작 일시

    private LocalDateTime endAt;        // 종료 일시
}
