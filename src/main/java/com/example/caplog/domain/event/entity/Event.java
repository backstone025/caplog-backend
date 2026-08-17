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
    private Long eventId;

    @JoinColumn(name = "schedule_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Schedule schedule;

    @JoinColumn(name = "image_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Images images;

    private String title;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String videoUrl;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}