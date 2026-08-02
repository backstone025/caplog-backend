package com.example.caplog.domain.users.dto;

public record UsersAuthRequest(
        String username,    // 사용자 아이디
        String password     // 사용자 비밀번호
) {
}
