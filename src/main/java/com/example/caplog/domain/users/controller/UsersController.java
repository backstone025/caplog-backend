package com.example.caplog.domain.users.controller;

import com.example.caplog.domain.users.dto.UsersAuthRequest;
import com.example.caplog.domain.users.dto.UsersAuthResponse;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UsersAuthResponse>> login(@RequestBody UsersAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(usersService.login(request)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UsersAuthResponse>> signup(@RequestBody UsersAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(usersService.signup(request)));
    }

}
