package com.example.caplog.domain.schedule.entity;

import com.example.caplog.domain.schedule.type.Category;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    Users user;

    @Enumerated(EnumType.STRING)
    Category category;

    LocalDateTime startTime;

    LocalDateTime endTime;

    String title;

    String description;
}
