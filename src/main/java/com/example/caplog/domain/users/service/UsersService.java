package com.example.caplog.domain.users.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.dto.GetUserInfoResponse;
import com.example.caplog.domain.users.dto.UsersPhotoConsentRequest;
import com.example.caplog.domain.users.dto.UsersPhotoConsentResponse;
import com.example.caplog.domain.users.dto.UsersProfileInfoResponse;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.repository.UsersDetailsRepository;
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
}
