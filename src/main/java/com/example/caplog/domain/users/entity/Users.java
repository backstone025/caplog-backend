package com.example.caplog.domain.users.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
    public static Users createUsers(String username, String password) {
        Users user = new Users();
        user.loginId = username;
        user.password = password;
        user.createAt = LocalDateTime.now();
        return user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersId;           // 사용자 아이디

    private String loginId;         // 사용자 로그인 아이디

    private String password;        // 비밀번호

    private LocalDateTime createAt; // 생성일시
}
