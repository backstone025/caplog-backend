package com.example.caplog.domain.images.service;

import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.repository.ImagesRepository;
import com.example.caplog.domain.images.type.ImageStatus;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import com.example.caplog.global.S3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImagesService {

    private final ImagesRepository imagesRepository;
    private final S3Service s3Service;

    @Transactional
    public Images upload(
            MultipartFile file,
            Users user
    ) {
        String imageKey = s3Service.upload(
                file,
                user.getUsersId()
        );

        Images image = Images.builder()
                .user(user)
                .ocrText(null)
                .imageStatus(ImageStatus.PENDING)
                .imageKey(imageKey)
                .build();

        return imagesRepository.save(image);
    }

    @Transactional
    public void startProcessing(Long imageId) {

        Images image = getImage(imageId);

        image.updateStatus(
                ImageStatus.PROCESSING
        );
    }

    @Transactional
    public void completeProcessing(
            Long imageId,
            String ocrText
    ) {

        Images image = getImage(imageId);

        image.updateOcrText(ocrText);
        image.updateStatus(
                ImageStatus.COMPLETED
        );
    }

    @Transactional
    public void failProcessing(Long imageId) {

        Images image = getImage(imageId);

        image.updateStatus(
                ImageStatus.FAILED
        );
    }

    @Transactional
    public void delete(Long imageId) {

        Images image = getImage(imageId);

        s3Service.delete(image.getImageKey());

        imagesRepository.delete(image);
    }

    private Images getImage(Long imageId) {

        return imagesRepository.findById(imageId)
                .orElseThrow(() ->
                        new GeneralException(
                                GlobalErrorCode.IMAGE_NOT_FOUND
                        )
                );
    }
}