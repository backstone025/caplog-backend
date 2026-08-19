package com.example.caplog.domain.auth.controller;

import com.example.caplog.domain.auth.dto.UsersAuthRequest;
import com.example.caplog.domain.auth.dto.UsersAuthResponse;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UsersAuthResponse>> login(@RequestBody UsersAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UsersAuthResponse>> signup(@RequestBody UsersAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request)));
    }

}
