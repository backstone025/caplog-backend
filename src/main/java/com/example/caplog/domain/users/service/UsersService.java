package com.example.caplog.domain.users.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.users.dto.GetUserInfoResponse;
import com.example.caplog.domain.users.dto.UsersPhotoConsentRequest;
import com.example.caplog.domain.users.dto.UsersPhotoConsentResponse;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.repository.UsersDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final AuthService authService;
    private final UsersDetailsRepository usersDetailsRepository;

    public GetUserInfoResponse getUserInfo() {
        return new GetUserInfoResponse(authService.getCurrentUser().getLoginId());
    }

    public UsersPhotoConsentResponse putUsersPhotoConsent(UsersPhotoConsentRequest request) {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));

        usersDetails.updatePhotoConsent(request.isApproved());
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }

    public UsersPhotoConsentResponse getUsersPhotoConsent() {
        Users user = authService.getCurrentUser();
        UsersDetails usersDetails = usersDetailsRepository.findById(user.getUsersId())
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다. id=" + user.getUsersId()));
        return new UsersPhotoConsentResponse(usersDetails.isPhotoConsent());
    }
}
