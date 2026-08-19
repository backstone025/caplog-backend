package com.example.caplog.domain.schedule.entity;

import com.example.caplog.domain.groups.entity.Groups;
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
    public boolean checkScheduleRecentlyUpdated() {
        LocalDateTime updateTime = this.getUpdatedAt();
        if (updateTime != null) {
            // 12시간 전 < 업데이트 시기 < 지금 시각
            return updateTime.isBefore(LocalDateTime.now())
                    && updateTime.isAfter(LocalDateTime.now().plusHours(12));
        }
        return false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;            // 일정 아이디

    @JoinColumn(name = "group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)      // DB의 'On Delete Cascade' 기능 활성화
    private Groups groups;              // 그룹

    private String title;   // 일정 제목

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private LocalDateTime viewedAt;     // 열람 일시

    private LocalDateTime createdAt;    // 생성 일시

    private LocalDateTime updatedAt;    // 수정 일시

    public void updateSchedule(
            String title,
            String aiSummary
    ) {
        this.title = title;
        this.aiSummary = aiSummary;
    }

    public void changeGroup(Groups groups) {
        this.groups = groups;
    }
}
