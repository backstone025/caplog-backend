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
        log.info("[UploadService] 파일 업로드 요청 - 파일명: {}, 파일 크기: {} bytes",
                file.getOriginalFilename(), file.getSize());

        Users currentUser = authService.getCurrentUser();
        log.info("[UploadService] 사용자 인증 완료 - UsersId: {}, LoginId: {}",
                currentUser.getUsersId(), currentUser.getLoginId());

        Images image = imagesService.upload(file, currentUser);
        log.info("[UploadService] 이미지 S3 업로드 및 DB 저장 완료 - ImageId: {}, ImageKey: {}",
                image.getImageId(), image.getImageKey());

        log.info("[UploadService] AI 이미지 분석 연동 시작 - ImageId: {}", image.getImageId());
        UploadResponse response = aiExtractService.processImageAnalysis(image, currentUser.getUsersId());
        log.info("[UploadService] AI 이미지 분석 완료 - ImageId: {}, 추출된 이벤트 수: {}",
                image.getImageId(), response.events() != null ? response.events().size() : 0);

        return response;
    }
}