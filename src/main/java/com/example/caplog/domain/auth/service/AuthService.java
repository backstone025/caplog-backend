package com.example.caplog.domain.auth.service;

import com.example.caplog.domain.auth.dto.UsersAuthRequest;
import com.example.caplog.domain.auth.dto.UsersAuthResponse;
import com.example.caplog.domain.auth.exception.AuthException;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.repository.UsersDetailsRepository;
import com.example.caplog.domain.users.repository.UsersRepository;
import com.example.caplog.global.config.auth.JwtProvider;
import com.example.caplog.global.error.exception.GeneralException;
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
public class AuthService {
    private final UsersRepository usersRepository;
    private final UsersDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public Long getUserId() {
        return (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getDetails();
    }

    //id로 실제 user 엔티티 조회
    public Users getCurrentUser() {
        Long userId = getUserId();

        return usersRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "존재하지 않는 사용자입니다."
                        )
                );
    }

    public UsersAuthResponse login(UsersAuthRequest request) {
        // 1. 인증 위임 (검증 및 인증 객체 생성)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. 인증된 객체에서 username을 추출 (DB에서 안전하게 다시 조회)
        Users user = usersRepository.findByLoginId(authentication.getName());

        // 3. 토큰 발행
        String accessToken = jwtProvider.createToken(authentication, 3600L * 24 * 7, user.getUsersId());

        return new UsersAuthResponse(accessToken);
    }

    public UsersAuthResponse signup(UsersAuthRequest request) {
        // 1. 검증
        checkUsernameExists(request.username());
        checkUserLoginIdForm(request.username());
        checkUserPasswordForm(request.password());
        String encodedPassword = passwordEncoder.encode(request.password());

        // 2. 저장
        // Users 객체 생성
        Users user = Users.createUsers(request.username(), encodedPassword);
        // UsersDetails 객체 생성
        UsersDetails usersDetails = UsersDetails.createUsersDetails(user);
        // DB 저장
        usersRepository.save(user);
        userDetailsRepository.save(usersDetails);

        // 3. 반환
        return login(request);
    }

    // 중복된 사용자에 대한 검증
    private void checkUsernameExists(String username) {
        Users user = usersRepository.findByLoginId(username);
        if (user != null) { // 유저가 존재하면 중복이므로 에러!
            throw new GeneralException(AuthException.LOGIN_ID_ALREADY_EXIST);
        }
    }

    // 로그인 아이디에 대한 검증
    private void checkUserLoginIdForm(String loginId){
        // 공백 체크
        if(loginId == null || loginId.isBlank()){
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
        // 한글, 영어(대소문자) 20자 이내
        String regex = "^[a-zA-Z가-힣]{1,20}$";

        if(!loginId.matches(regex)){
            throw new GeneralException(AuthException.LOGIN_ID_BAD_FORM);
        }
    }

    // 비밀번호에 대한 검증
    private  void checkUserPasswordForm(String password){
        // 공백 체크
        if(password == null || password.isBlank()){
            throw new GeneralException(AuthException.PASSWORD_BAD_FORM);
        }

        // 숫자 4자리
        String regex = "^[0-9]{4}$";
        if(!password.matches(regex)){
            throw new GeneralException(AuthException.PASSWORD_BAD_FORM);
        }
    }
}
