package com.example.caplog.domain.groups.entity;

import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "groups_table")
public class Groups {

    public static Groups createGroups(Users user, String title, Category category) {
        Groups group = new Groups();
        group.user = user;
        group.title = title;
        group.category = category;
        return group;
    }

    public void updateGroups(Users user, String title, Category category) {
        this.user = user;
        this.title = title;
        this.category = category;
    }
    // 그룹 상세 열람
    public void markAsViewed() {
        this.viewedAt = LocalDateTime.now();
    }

    // 그룹에 새로운 정보가 추가되었을 때 호출
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // 아직 열람하지 않았거나,
    // 마지막 열람 이후 새로운 정보가 추가/수정되었으면 NEW
    public boolean isNew() {
        if (viewedAt == null) {
            return true;
        }

        return updatedAt != null &&
                updatedAt.isAfter(viewedAt);
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;               // 그룹 아이디

    @JoinColumn(name = "user_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;                 // 사용자

    private String title;               // 그룹명

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime viewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
