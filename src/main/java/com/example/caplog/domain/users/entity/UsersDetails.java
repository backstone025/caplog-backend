package com.example.caplog.domain.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersDetails {

    @Id
    private Long userId;                // Users의 PK(usersId)를 그대로 PK로 사용

    @MapsId                             // Users의 PK를 이 엔티티의 PK(id)이자 FK로 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")      // DB FK 컬럼명
    private Users user;

    private boolean photoConsent;       // 사진 수집 동의 여부

    private LocalDateTime updateAt;     // 사용자 세부 정보 업데이트 일시

    // 생성 메서드
    public static UsersDetails createUsersDetails(Users user, boolean photoConsent) {
        UsersDetails details = new UsersDetails();
        details.user = user;
        details.photoConsent = photoConsent;
        details.updateAt = LocalDateTime.now();
        return details;
    }

    // 상태 변경 메서드 (동의 여부 변경)
    public void updatePhotoConsent(boolean photoConsent) {
        this.photoConsent = photoConsent;
    }
}