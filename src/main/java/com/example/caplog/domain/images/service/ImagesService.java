package com.example.caplog.domain.images.service;

import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import com.example.caplog.domain.ai.chat.service.ChatService;
import com.example.caplog.domain.ai.vector.VectorService;
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
import com.example.caplog.domain.ai.chat.service.ChatService;

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
    private final VectorService vectorService;

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
        if (image.getImageStatus() == ImageStatus.COMPLETED) {
            return s3Service.getUrl(image.getImageKey());
        }
        return null;
    }

    @Transactional
    public Long confirmUpload(
            UploadConfirmRequest request
    ) {

        Users user = authService.getCurrentUser();

        // 1. 업로드한 이미지 조회
        Images image =
                imagesRepository.findById(request.imageId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "이미지를 찾을 수 없습니다."
                                )
                        );

        // 다른 사용자의 이미지 접근 방지
        if (!image.getUser()
                .getUsersId()
                .equals(user.getUsersId())) {

            throw new IllegalArgumentException(
                    "유효하지 않은 이미지입니다."
            );
        }


        // 2. 카테고리 변환
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


        // targetType 필수
        if (request.targetType() == null) {

            throw new IllegalArgumentException(
                    "저장할 주제 유형이 필요합니다."
            );
        }


        // 3. 저장될 그룹 결정
        Groups group = null;


        /*
         * ==============================
         * CASE 1. 기존 그룹 선택
         * targetType = GROUP
         * targetId = groupId
         * ==============================
         */
        if (request.targetType()
                == UploadConfirmRequest.TargetType.GROUP) {

            if (request.targetId() == null) {

                throw new IllegalArgumentException(
                        "그룹 아이디가 필요합니다."
                );
            }

            group =
                    groupsRepository.findById(
                                    request.targetId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "그룹을 찾을 수 없습니다."
                                    )
                            );


            // 다른 사용자의 그룹 접근 방지
            if (!group.getUser()
                    .getUsersId()
                    .equals(user.getUsersId())) {

                throw new IllegalArgumentException(
                        "유효하지 않은 그룹입니다."
                );
            }
        }


        /*
         * ==============================
         * CASE 2. 기존 단일정보 선택
         *
         * targetType = SCHEDULE
         * targetId = 기존 단일정보 scheduleId
         *
         * 기존 단일정보 + 새 정보
         * → 새로운 그룹 생성
         * ==============================
         */
        else if (
                request.targetType()
                        == UploadConfirmRequest.TargetType.SCHEDULE
        ) {

            if (request.targetId() == null) {

                throw new IllegalArgumentException(
                        "기존 단일정보 아이디가 필요합니다."
                );
            }


            // 기존 단일정보 조회
            Schedule existingSchedule =
                    scheduleRepository
                            .findByScheduleIdAndUser(
                                    request.targetId(),
                                    user
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "기존 단일정보를 찾을 수 없습니다."
                                    )
                            );


            // 기존 단일정보여야 함
            // 이미 그룹에 들어있는 Schedule이면 잘못된 요청
            if (existingSchedule.getGroups() != null) {

                throw new IllegalArgumentException(
                        "이미 그룹에 속한 정보입니다."
                );
            }


            String groupTitle =
                    chatService.generateGroupTitle(
                            existingSchedule.getTitle(),
                            existingSchedule.getAiSummary(),
                            request.title(),
                            request.aiSummary()
                    );


            // 새로운 그룹 생성
            Groups newGroup =
                    Groups.createGroups(
                            user,
                            groupTitle,
                            category
                    );


            // DB에 그룹 저장
            newGroup =
                    groupsRepository.save(
                            newGroup
                    );


            // 기존 단일정보를 새 그룹으로 이동
            existingSchedule.changeGroup(
                    newGroup
            );


            /*
             * 새 그룹이 최초 생성됐으므로
             * Qdrant Vector 저장
             *
             * 기존 그룹에 정보 추가할 때는 호출 X
             */
            vectorService.saveGroupsVector(
                    newGroup
            );


            // 새로 저장되는 Schedule 역시
            // 같은 그룹에 들어가도록 지정
            group = newGroup;
        }


        /*
         * ==============================
         * CASE 3. 단일정보로 저장
         *
         * targetType = NONE
         * targetId = null
         * ==============================
         */
        else if (
                request.targetType()
                        == UploadConfirmRequest.TargetType.NONE
        ) {

            if (request.targetId() != null) {

                throw new IllegalArgumentException(
                        "단일정보 저장 시 targetId는 사용할 수 없습니다."
                );
            }

            group = null;
        }


        // 4. 새로운 Schedule 생성
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


        // 5. Event 생성
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

                eventRepository.save(
                        event
                );
            }
        }


        /*
         * 6. 기존 그룹에 새 정보가 들어간 경우에만
         * 그룹 updatedAt 갱신
         *
         * SCHEDULE 타입은 새 그룹을 만든 것이기 때문에
         * touch() 필요 없음.
         */
        if (request.targetType()
                == UploadConfirmRequest.TargetType.GROUP
                && group != null) {

            group.touch();
        }


        // 7. 이미지 처리 완료
        image.updateStatus(
                ImageStatus.COMPLETED
        );


        // 8. 새로 생성된 Schedule ID 반환
        return savedSchedule.getScheduleId();
    }


    /*
     * 요청으로 들어온 날짜 String을
     * DB 저장용 LocalDateTime으로 변환
     */
    private LocalDateTime parseDateTime(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm:ss"
                    );

            return LocalDateTime.parse(
                    value,
                    formatter
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "올바르지 않은 날짜 형식입니다."
            );
        }
    }
}