package com.example.caplog.domain.upload.service;

import com.example.caplog.domain.ai.chat.service.AiExtractService;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.upload.dto.UploadResponse;
import com.example.caplog.domain.users.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UploadService {

    private final AuthService authService;
    private final AiExtractService aiExtractService;
    private final ImagesService imagesService;

    public UploadResponse upload(MultipartFile file) {
        Users currentUser = authService.getCurrentUser();
        Images image = imagesService.upload(file, currentUser);

        return aiExtractService.processImageAnalysis(image, currentUser.getUsersId());
    }
}