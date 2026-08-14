package com.example.caplog.domain.users.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.users.dto.GetUserInfoResponse;
import com.example.caplog.domain.users.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final AuthService authService;

    public GetUserInfoResponse getUserInfo(){
        return new GetUserInfoResponse(authService.getCurrentUser().getLoginId());
    }
}
