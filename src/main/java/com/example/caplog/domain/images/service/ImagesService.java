package com.example.caplog.domain.images.service;

import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import com.example.caplog.domain.ai.chat.service.ChatService;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.images.dto.ImageUploadResponse;
import com.example.caplog.domain.images.dto.request.UploadConfirmRequest;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.repository.ImagesRepository;
import com.example.caplog.domain.images.type.ImageStatus;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import com.example.caplog.global.S3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImagesService {

    private final ImagesRepository imagesRepository;
    private final S3Service s3Service;
    private final ChatService chatService;
    private final AuthService authService;
    private final GroupsRepository groupsRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;

    @Transactional
    public ImageUploadResponse upload(
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

            return ImageUploadResponse.from(
                    image,
                    result
            );

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

    @Transactional
    public Long confirmUpload(
            UploadConfirmRequest request
    ) {

        Users user =
                authService.getCurrentUser();

        Images image =
                imagesRepository.findById(request.imageId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "이미지를 찾을 수 없습니다."
                                )
                        );

        Groups group = null;

        if (request.groupId() != null) {
            group =
                    groupsRepository.findById(
                                    request.groupId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "그룹을 찾을 수 없습니다."
                                    )
                            );
        }

        Category category;

        try {
            category =
                    Category.valueOf(
                            request.category()
                                    .toUpperCase()
                    );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 카테고리입니다."
            );
        }

        Schedule schedule =
                Schedule.createSchedule(
                        user,
                        group,
                        request.title(),
                        request.aiSummary(),
                        category
                );

        Schedule savedSchedule =
                scheduleRepository.save(
                        schedule
                );

        if (request.events() != null) {

            for (UploadConfirmRequest.EventRequest eventRequest
                    : request.events()) {

                Event event =
                        Event.createEvent(
                                savedSchedule,
                                image,
                                eventRequest.title(),
                                eventRequest.location(),
                                eventRequest.details(),
                                parseDateTime(
                                        eventRequest.startAt()
                                ),
                                parseDateTime(
                                        eventRequest.endAt()
                                )
                        );

                eventRepository.save(event);
            }
        }

        if (group != null) {
            group.touch();
        }

        image.updateStatus(
                ImageStatus.COMPLETED
        );

        return savedSchedule.getScheduleId();
    }

    private LocalDateTime parseDateTime(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"
                );

        return LocalDateTime.parse(
                value,
                formatter
        );
    }
}