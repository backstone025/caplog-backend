package com.example.caplog.domain.schedule.entity;

import com.example.caplog.domain.groups.entity.Groups;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;            // 일정 아이디

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Groups groups;              // 그룹

    private String title;               // 일정 제목

    private LocalDateTime viewedAt;     // 열람 일시

    private LocalDateTime createdAt;    // 생성 일시

    private LocalDateTime updatedAt;    // 수정 일시
}
