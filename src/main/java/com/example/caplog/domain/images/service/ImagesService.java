package com.example.caplog.domain.images.service;

import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import com.example.caplog.domain.ai.chat.service.ChatService;
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
    private final ChatService chatService;

    @Transactional
    public AiChatResponse upload(
            MultipartFile file,
            Users user
    ) {

        String imageKey = s3Service.upload(
                file,
                user.getUsersId()
        );

        Images image = Images.builder()
                .user(user)
                .imageStatus(ImageStatus.PENDING)
                .imageKey(imageKey)
                .build();

        imagesRepository.save(image);

        try {

            byte[] imageBytes =
                    s3Service.download(imageKey);

            AiChatResponse result =
                    chatService.analyzeImage(
                            imageBytes,
                            file.getContentType()
                    );

            image.updateOcrText(
                    result.extractedText()
            );

            image.updateStatus(
                    ImageStatus.COMPLETED
            );

            return result;

        } catch (Exception e) {

            image.updateStatus(
                    ImageStatus.FAILED
            );
            throw e;
        }
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

    // Images의 id -> 이미지 Full URL 출력하는 메소드
    @Transactional
    public String getUrl(Images image) {
        // 이미지 정보가 없을 경우
        if (image == null || image.getImageKey() == null) {
            return null;
        }
        // 이미지 상태가 완료 상태일 경우만 URL 추출 시도
        if(image.getImageStatus() == ImageStatus.COMPLETED){
            return s3Service.getUrl(image.getImageKey());
        }
        return null;
    }
}