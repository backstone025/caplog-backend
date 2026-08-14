package com.example.caplog.domain.users.controller;

import com.example.caplog.domain.users.dto.GetUserInfoResponse;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
