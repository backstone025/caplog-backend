package com.example.caplog.domain.users.dto;

import com.example.caplog.domain.users.type.ProfileImage;

public record UsersProfileInfoRequest(
        String username,            // 수정할 사용자 로그인 아이디
        String profileImg     // 수정할 프로필 이미지 타입
) {
}
