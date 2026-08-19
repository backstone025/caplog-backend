package com.example.caplog.domain.auth.dto;

public record UsersAuthRequest(
        String userName,    // 사용자 아이디
        String password     // 사용자 비밀번호
) {
}
