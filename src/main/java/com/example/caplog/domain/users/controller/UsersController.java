package com.example.caplog.domain.users.controller;

import com.example.caplog.domain.users.dto.GetUserInfoResponse;
import com.example.caplog.domain.users.dto.UsersPhotoConsentRequest;
import com.example.caplog.domain.users.dto.UsersPhotoConsentResponse;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetUserInfoResponse>>getUserInfo(){
        GetUserInfoResponse response = usersService.getUserInfo();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/photo-consent")
    public ResponseEntity<ApiResponse<UsersPhotoConsentResponse>> putPhotoConsent(
            @RequestBody UsersPhotoConsentRequest request
    ){
        UsersPhotoConsentResponse response = usersService.putUsersPhotoConsent(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/photo-consent")
    public ResponseEntity<ApiResponse<UsersPhotoConsentResponse>> getPhotoConsent(){
        UsersPhotoConsentResponse response = usersService.getUsersPhotoConsent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
