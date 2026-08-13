package com.example.caplog.domain.groups.entity;

import com.example.caplog.domain.schedule.type.Category;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "groups_table")
public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;               // 그룹 아이디

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;                 // 사용자

    private String title;               // 그룹명

    @Enumerated(EnumType.STRING)
    private Category category;          // 카테고리
}
