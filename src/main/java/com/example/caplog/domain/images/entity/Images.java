package com.example.caplog.domain.images.entity;

import com.example.caplog.domain.images.type.ImageStatus;
import com.example.caplog.domain.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Images {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_status", nullable = false, length = 20)
    private ImageStatus imageStatus;

    @Column(name = "image_key", nullable = false, length = 500)
    private String imageKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Images(
            Users user,
            String ocrText,
            ImageStatus imageStatus,
            String imageKey
    ) {
        this.user = user;
        this.ocrText = ocrText;
        this.imageStatus = imageStatus;
        this.imageKey = imageKey;
        this.createdAt = LocalDateTime.now();
    }

    public void updateOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public void updateStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }
}
