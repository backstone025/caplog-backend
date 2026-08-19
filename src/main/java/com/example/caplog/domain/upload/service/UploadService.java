package com.example.caplog.domain.upload.service;

import com.example.caplog.domain.ai.chat.service.AiExtractService;
import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.repository.GroupsRepository;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.repository.ImagesRepository;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.upload.dto.ConfirmRequest;
import com.example.caplog.domain.upload.dto.UploadResponse;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UploadService {

    private final GroupsRepository groupsRepository;
    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final ImagesRepository imagesRepository;

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
        UploadResponse response = aiExtractService.processImageAnalysis(file, image.getImageId(), currentUser.getUsersId());
        log.info("[UploadService] AI 이미지 분석 완료 - ImageId: {}, 추출된 이벤트 수: {}",
                image.getImageId(), response.events() != null ? response.events().size() : 0);

        return response;
    }

    @Transactional
    public void confirmSchedule(ConfirmRequest request) {
        Users user = authService.getCurrentUser();
        log.info("[UploadService] 일정 확정 저장 시작 - UserId: {}, ImageId: {}", user.getUsersId(), request.imageId());

        // 1. 이미지 조회
        Images image = imagesRepository.findById(request.imageId())
                .orElseThrow(() -> new GeneralException(GlobalErrorCode.IMAGE_NOT_FOUND));

        // 2. Groups 처리 (기존 그룹 매칭 OR 신규 그룹 생성 OR null)
        Groups group = resolveGroup(request, user);

        // 3. Schedule 생성 및 저장
        Schedule schedule = Schedule.createSchedule(
                group,
                request.title(),
                request.scheduleAiSummary()
        );
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 4. 체크된 Event 항목들만 필터링하여 생성 및 저장
        if (request.events() != null && !request.events().isEmpty()) {
            List<Event> eventsToSave = request.events().stream()
                    .filter(ConfirmRequest.EventConfirmDto::isChecked) // isChecked가 true인 것만 선택
                    .map(eventDto -> Event.createEvent(
                            savedSchedule,
                            image,
                            eventDto.title(),
                            eventDto.details(),
                            eventDto.aiSummary(),
                            eventDto.videoUrl(),
                            parseToLocalDateTime(eventDto.startAt()),
                            parseToLocalDateTime(eventDto.endAt())
                    ))
                    .toList();

            eventRepository.saveAll(eventsToSave);
            log.info("[UploadService] 총 {}개의 Event 저장 완료", eventsToSave.size());
        }
    }

    private Groups resolveGroup(ConfirmRequest request, Users user) {
        // 1. 기존 groupId가 전달되었고 0보다 큰 유효한 ID인 경우 조회
        if (request.groupId() != null && request.groupId() > 0) {
            return groupsRepository.findById(request.groupId()).orElse(null);
        }

        // 2. groupId가 0이거나 null이지만, 그룹명(group)이 입력되어 들어온 경우 신규 그룹 생성
        if (request.group() != null && !request.group().isBlank()) {
            Groups newGroup = Groups.createGroups(user, request.group(), request.category());
            return groupsRepository.save(newGroup);
        }

        // 3. 그룹 선택/입력이 없는 경우
        Groups newGroup = Groups.createGroups(user, request.title(), request.category());
        return groupsRepository.save(newGroup);
    }

    private LocalDateTime parseToLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("[UploadService] 날짜 파싱 실패: {}", dateStr);
            return null;
        }
    }
}