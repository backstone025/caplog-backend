package com.example.caplog.domain.schedule.entity;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.users.entity.Users;
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
public class Schedule {
    public static Schedule createSchedule(
            Users user,
            Groups groups,
            String title,
            String aiSummary,
            Category category
    ) {
        Schedule schedule = new Schedule();

        schedule.user = user;
        schedule.groups = groups;
        schedule.title = title;
        schedule.aiSummary = aiSummary;
        schedule.category = category;

        return schedule;
    }

    public void updateSchedule(
            Groups groups,
            String title,
            String aiSummary,
            Category category
    ) {
        this.groups = groups;
        this.title = title;
        this.aiSummary = aiSummary;
        this.category = category;
    }

    public void markAsViewed() {
        this.viewedAt = LocalDateTime.now();
    }

    public boolean isNew() {

        if (viewedAt == null) {
            return true;
        }

        return updatedAt != null &&
                updatedAt.isAfter(viewedAt);
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;            // 일정 아이디

    @JoinColumn(name = "user_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)      // DB의 'On Delete Cascade' 기능 활성화
    private Groups groups;              // 그룹

    private String title;// 일정 제목

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime viewedAt;     // 열람 일시

    private LocalDateTime createdAt;    // 생성 일시

    private LocalDateTime updatedAt;    // 수정 일시
}
