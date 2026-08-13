package com.example.caplog.domain.images.entity;

import com.example.caplog.domain.images.type.ImageStatus;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;               // 이미지 아이디

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;                 // 사용자

    private String ocrText;             // 추출 텍스트

    @Enumerated(EnumType.STRING)
    private ImageStatus imageStatus;    // 처리 상태

    private String imageKey;            // 이미지 키

    private LocalDateTime createdAt;    // 생성 일시
}
