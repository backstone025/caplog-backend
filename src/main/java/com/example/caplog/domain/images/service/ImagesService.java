package com.example.caplog.domain.images.service;

import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.repository.ImagesRepository;
import com.example.caplog.domain.images.type.ImageStatus;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import com.example.caplog.global.S3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ★ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j // ★ 추가
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImagesService {

    private final ImagesRepository imagesRepository;
    private final S3Service s3Service;

    @Transactional
    public Images upload(MultipartFile file, Users user) {
        log.info("[ImagesService] S3 업로드 시작 - File: {}, UserId: {}", file.getOriginalFilename(), user.getUsersId());

        String imageKey = s3Service.upload(file, user.getUsersId());

        log.info("[ImagesService] S3 업로드 완료 - ImageKey: {}", imageKey);

        Images image = Images.builder()
                .user(user)
                .imageStatus(ImageStatus.PENDING)
                .imageKey(imageKey)
                .build();

        Images savedImage = imagesRepository.save(image);
        log.info("[ImagesService] DB 저장 완료 - ImageId: {}", savedImage.getImageId());

        return savedImage;
    }

    @Transactional
    public void startProcessing(Long imageId) {
        Images image = getImage(imageId);
        image.updateStatus(ImageStatus.PROCESSING);
    }

    @Transactional
    public void completeProcessing(Long imageId, String ocrText) {
        Images image = getImage(imageId);
        image.updateOcrText(ocrText);
        image.updateStatus(ImageStatus.COMPLETED);
    }

    @Transactional
    public void failProcessing(Long imageId) {
        Images image = getImage(imageId);
        image.updateStatus(ImageStatus.FAILED);
    }

    @Transactional
    public void delete(Long imageId) {
        Images image = getImage(imageId);
        s3Service.delete(image.getImageKey());
        imagesRepository.delete(image);
    }

    private Images getImage(Long imageId) {
        return imagesRepository.findById(imageId)
                .orElseThrow(() -> new GeneralException(GlobalErrorCode.IMAGE_NOT_FOUND));
    }

    @Transactional
    public String getUrl(Images image) {
        if (image == null || image.getImageKey() == null) {
            return null;
        }
        if (image.getImageStatus() == ImageStatus.COMPLETED) {
            return s3Service.getUrl(image.getImageKey());
        }
        return null;
    }
}