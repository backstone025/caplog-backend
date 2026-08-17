package com.example.caplog.domain.users.service;

import com.example.caplog.domain.auth.exception.AuthException;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.UsersException;
import com.example.caplog.domain.users.dto.request.UsersAlarmInfoRequest;
import com.example.caplog.domain.users.dto.request.UsersPhotoConsentRequest;
import com.example.caplog.domain.users.dto.request.UsersProfileInfoRequest;
import com.example.caplog.domain.users.dto.response.*;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.repository.UsersDetailsRepository;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.domain.users.type.ProfileImage;
import com.example.caplog.global.S3.S3Service;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final AuthService authService;
    private final UsersRepository usersRepository;
    private final UsersDetailsRepository usersDetailsRepository;
    private final ScheduleRepository scheduleRepository;
    private final S3Service s3Service;

    // 현재 로그인한 사용자의 UserDetails를 가져오는 메소드
    public UsersDetails getUsersDetails() {
        return usersDetailsRepository.findById(authService.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
    }

    // 모든 사용자의 세부 정보를 가져오는 메소드
    public List<UsersDetails> getAllUsersDetails() {
        return usersDetailsRepository.findAll();
    }

    // 잦은 DB 접속 방지를 위해 한번에 Users를 가져오는 메소드
    public Map<Long, Users> getUsersMap() {
        return usersRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Users::getUsersId,
                        user -> user
                ));
    }

    // #1-1 로그인한 사용자 정보 조회
    public GetUserInfoResponse getUserInfo() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = this.getUsersDetails();

        // 사용자 로그인 아이디
        String username = user.getLoginId();

        // 사용자 프로필 이미지 URL
        String imgUrl = s3Service.getUrl(usersDetails.getProfileImage().getKey());

        // 사용자의 전체 일정 개수
        Integer totalSchedule = scheduleRepository.countAllByUser(user);

        // 이번 달 등록한 일정 개수
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startDate = currentYearMonth.atDay(1).atStartOfDay();                      // 이번 달 1일(00:00:00)
        LocalDateTime endDate = currentYearMonth.atEndOfMonth().atTime(23, 59, 59);     // 이번 달 말일(23:59:59)
        Integer thisMonthSchedule = scheduleRepository
                .countByUserAndCreatedAtBetween(user, startDate, endDate);

        return new GetUserInfoResponse(username, imgUrl, totalSchedule, thisMonthSchedule);
    }

    // #1-2 사용자 사진 접근 동의 확정
    public UsersPhotoConsentResponse putUsersPhotoConsent(UsersPhotoConsentRequest request) {
        UsersDetails usersDetails = this.getUsersDetails();

        usersDetails.updatePhotoConsent(request.isApproved());
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }

    // #1-3 사용자 사진 접근 동의 확정
    public UsersPhotoConsentResponse getUsersPhotoConsent() {
        UsersDetails usersDetails = this.getUsersDetails();
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }

    // #1-4-1 사용자 프로필 정보 조회
    public UsersProfileInfoResponse getUserProfileInfo() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = this.getUsersDetails();
        return new UsersProfileInfoResponse(user.getLoginId(), usersDetails.getProfileImage());
    }

    // #1-4-2 사용자 프로필 수정
    public UsersProfileInfoResponse updateUserProfileInfo(UsersProfileInfoRequest request) {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = this.getUsersDetails();

        // 로그인 아이디 업데이트
        String loginId = request.username();
        checkUserLoginIdForm(loginId);
        user.updateLoginId(loginId);

        // 프로필 이미지 업데이트
        checkUserProfileImageType(request.profileImg());
        ProfileImage profileImage = ProfileImage.valueOf(request.profileImg());
        usersDetails.updateProfileImage(profileImage);

        return new UsersProfileInfoResponse(loginId, profileImage);
    }

    // 로그인 아이디에 대한 검증
    private void checkUserLoginIdForm(String loginId) {
        // 공백 체크
        if (loginId == null || loginId.isBlank()) {
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
        // 한글, 영어(대소문자) 20자 이내
        String regex = "^[a-zA-Z가-힣]{1,20}$";

        if (!loginId.matches(regex)) {
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
    }

    // 프로필 이미지 타입에 대한 검증
    private void checkUserProfileImageType(String profileImage) {
        if (!ProfileImage.isContain(profileImage)) {
            throw new GeneralException(UsersException.USERS_PROFILE_IMAGE_BAD_REQUEST);
        }
    }

    // #1-5-1 사용자 알림 설정 정보 조회
    public UsersAlarmInfoResponse getUsersAlarmConsent() {
        UsersDetails usersDetails = this.getUsersDetails();

        boolean totalAlarm;
        boolean imminentAlarm = usersDetails.isImminentAlarm();
        boolean unviewedAlarm = usersDetails.isUnviewedAlarm();
        boolean aiRecommendedAlarm = usersDetails.isAiRecommendedAlarm();
        // 만일 모든 알림을 받는다고 한다면
        totalAlarm = imminentAlarm && unviewedAlarm && aiRecommendedAlarm;

        return new UsersAlarmInfoResponse(totalAlarm, imminentAlarm, unviewedAlarm, aiRecommendedAlarm);
    }

    // #1-5-2 사용자 알림 설정
    public UsersAlarmInfoResponse updateUsersAlarmConsent(UsersAlarmInfoRequest request) {
        UsersDetails usersDetails = this.getUsersDetails();

        boolean totalAlarm;
        boolean imminentAlarm = request.imminentAlarm();
        boolean unviewedAlarm = request.unviewedAlarm();
        boolean aiRecommendedAlarm = request.aiRecommendedAlarm();
        // 만일 모든 알림을 받는다고 한다면
        totalAlarm = imminentAlarm && unviewedAlarm && aiRecommendedAlarm;

        // 상태 변경
        usersDetails.updateAlarmInfo(
                imminentAlarm,
                unviewedAlarm,
                aiRecommendedAlarm
        );

        return new UsersAlarmInfoResponse(totalAlarm, imminentAlarm, unviewedAlarm, aiRecommendedAlarm);
    }

    // #1-6 프로필 사진 URL 조회
    public UsersProfileImgUrlListResponse getUserProfileImgUrlList() {
        List<ProfileImage> profileImages = Arrays.stream(ProfileImage.values()).toList();

        Map<String, String> imageMapper = profileImages.stream()
                .collect(Collectors.toMap(
                        Enum::name,
                        img -> s3Service.getUrl(img.getKey())
                ));

        return UsersProfileImgUrlListResponse.from(profileImages, imageMapper);
    }

    // #1-7 FCM 토큰 갱신 메서드
    public void updateFcmToken(String fcmToken) {
        UsersDetails usersDetails = this.getUsersDetails();
        usersDetails.updateFcmToken(fcmToken); // Dirty Checking으로 자동 DB 반영
    }
}
