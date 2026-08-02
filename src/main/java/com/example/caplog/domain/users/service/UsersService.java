package com.example.caplog.domain.users.service;

import com.example.caplog.domain.users.dto.UsersAuthRequest;
import com.example.caplog.domain.users.dto.UsersAuthResponse;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.global.config.auth.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public Long getUserId() {
        return (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getDetails();
    }

    public UsersAuthResponse login(UsersAuthRequest request) {
        // 1. 인증 위임 (검증 및 인증 객체 생성)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. 인증된 객체에서 username을 추출 (DB에서 안전하게 다시 조회)
        Users user = usersRepository.findByUsername(authentication.getName());

        // 3. 토큰 발행
        String accessToken = jwtProvider.createToken(authentication, 3600L * 24 * 7, user.getId());

        return new UsersAuthResponse(accessToken);
    }

    public UsersAuthResponse signup(UsersAuthRequest request) {
        //1. 검증
        checkUsernameExists(request.username());
        String encodedPassword = passwordEncoder.encode(request.password());

        //2. 저장
        //Users 객체 생성
        Users user = Users.createUsers(request.username(), encodedPassword);
        //DB 저장
        usersRepository.save(user);

        //3. 반환
        return login(request);
    }

    // 중복된 사용자에 대한 검증
    private void checkUsernameExists(String username) {
        Users user = usersRepository.findByUsername(username);
        if (user != null) { // 유저가 존재하면 중복이므로 에러!
            throw new UsernameNotFoundException("존재하지 않는 사용자 아이디입니다. : " + username);
        }
    }
}
