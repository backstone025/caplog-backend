package com.example.caplog.domain.users.entity;

import com.example.caplog.domain.users.type.ProfileImage;
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

    // 프로파일 이미지
    @Enumerated(EnumType.STRING)
    private ProfileImage profileImage;  // 프로필 이미지 타입

    // 동의 여부
    private boolean photoConsent;       // 사진 수집 동의 여부

    private boolean alarmConsent;       // 알림 동의 여부

    // FCM Token
    private String fcmToken;

    // 알림 설정
    private boolean imminentAlarm;      // 암박한 알림 허용 여부

    private boolean unviewedAlarm;      // 미확인 알림 허용 여부

    private boolean aiRecommendedAlarm; // AI 추천 알림 혀용 여부

    // users 테이블 관련 날짜
    private LocalDateTime updateAt;     // 사용자 세부 정보 업데이트 일시

    // 정적 팩토리 메소드
    public static UsersDetails createUsersDetails(Users user) {
        UsersDetails details = new UsersDetails();
        details.user = user;
        details.profileImage = ProfileImage.randomSet();
        details.photoConsent = false;
        details.alarmConsent = false;
        details.imminentAlarm = true;
        details.unviewedAlarm = true;
        details.aiRecommendedAlarm = true;
        details.updateAt = LocalDateTime.now();
        return details;
    }

    // 상태 변경 메서드 (동의 여부 변경)
    public void updatePhotoConsent(boolean photoConsent) {
        this.photoConsent = photoConsent;
    }

    // 프로필 이미지 변경
    public void updateProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    // 알림 설정 변경
    public void updateAlarmInfo(boolean imminentAlarm, boolean unviewedAlarm, boolean aiRecommendedAlarm) {
        this.imminentAlarm = imminentAlarm;
        this.unviewedAlarm = unviewedAlarm;
        this.aiRecommendedAlarm = aiRecommendedAlarm;
    }
}