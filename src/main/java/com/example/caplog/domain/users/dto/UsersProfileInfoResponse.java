package com.example.caplog.domain.users.dto;

import com.example.caplog.domain.users.type.ProfileImage;

public record UsersProfileInfoResponse(
        String username,            // 사용자 로그인 아이디
        ProfileImage profileImg     // 프로필 이미지 타입
) {
}
