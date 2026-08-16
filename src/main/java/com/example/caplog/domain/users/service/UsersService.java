package com.example.caplog.domain.users.service;

import com.example.caplog.domain.auth.exception.AuthException;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.UsersException;
import com.example.caplog.domain.users.dto.*;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.repository.UsersDetailsRepository;
import com.example.caplog.domain.users.type.ProfileImage;
import com.example.caplog.global.S3.S3Service;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final AuthService authService;
    private final UsersDetailsRepository usersDetailsRepository;
    private final ScheduleRepository scheduleRepository;
    private final S3Service s3Service;

    // #1-1 로그인한 사용자 정보 조회
    public GetUserInfoResponse getUserInfo() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));

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
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));

        usersDetails.updatePhotoConsent(request.isApproved());
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }

    // #1-3 사용자 사진 접근 동의 확정
    public UsersPhotoConsentResponse getUsersPhotoConsent() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }

    // #1-4-1 사용자 프로필 정보 조회
    public UsersProfileInfoResponse getUserProfileInfo() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));
        return new UsersProfileInfoResponse(user.getLoginId(), usersDetails.getProfileImage());
    }

    // #1-4-2 사용자 프로필 수정
    public UsersProfileInfoResponse updateUserProfileInfo(UsersProfileInfoRequest request) {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));

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
    private void checkUserLoginIdForm(String loginId){
        // 공백 체크
        if(loginId == null || loginId.isBlank()){
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
        // 한글, 영어(대소문자) 20자 이내
        String regex = "^[a-zA-Z가-힣]{1,20}$";

        if(!loginId.matches(regex)){
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
    }

    // 프로필 이미지 타입에 대한 검증
    private void checkUserProfileImageType(String profileImage){
        if(!ProfileImage.isContain(profileImage)){
            throw new GeneralException(UsersException.USERS_PROFILE_IMAGE_BAD_REQUEST);
        }
    }
}
