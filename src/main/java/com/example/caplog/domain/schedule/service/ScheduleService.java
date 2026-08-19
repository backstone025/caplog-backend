package com.example.caplog.domain.schedule.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.schedule.dto.ScheduleDetailsResponse;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;
    private final AuthService authService;
    private final ImagesService imagesService;

    @Transactional(readOnly = true)
    public ScheduleDetailsResponse getScheduleDetails(Long scheduleId) {

        // 1. 현재 로그인 사용자
        Users currentUser = authService.getCurrentUser();

        // 2. 일정 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new GeneralException(GlobalErrorCode.SCHEDULE_NOT_FOUND)
                );

        // 3. 해당 일정의 Event + Images 조회
        List<Event> events =
                eventRepository.findAllByScheduleWithImages(schedule);

        // 4. 일정 소유자 검증
        validateScheduleOwner(schedule, events, currentUser);

        // 5. 이미지 URL 목록
        List<String> imageUrls = events.stream()
                .map(Event::getImages)
                .filter(Objects::nonNull)
                .distinct()
                .map(imagesService::getUrl)
                .toList();

        // 6. Event 응답 변환
        List<ScheduleDetailsResponse.EventInfo> eventInfos =
                events.stream()
                        .map(event -> {

                            boolean hasDate =
                                    event.getStartAt() != null
                                            || event.getEndAt() != null;

                            return new ScheduleDetailsResponse.EventInfo(
                                    event.getEventId(),
                                    event.getTitle(),
                                    hasDate,
                                    event.getStartAt(),
                                    event.getEndAt(),
                                    event.getDetails()
                            );
                        })
                        .toList();

        // 7. 최종 응답
        return new ScheduleDetailsResponse(
                schedule.getScheduleId(),
                imageUrls,
                schedule.getTitle(),
                schedule.getAiSummary(),
                eventInfos.size(),
                eventInfos
        );
    }


    private void validateScheduleOwner(
            Schedule schedule,
            List<Event> events,
            Users currentUser
    ) {

        /*
         * 1. 그룹에 포함된 일정
         *
         * Schedule → Groups → Users
         */
        Groups group = schedule.getGroups();

        if (group != null) {

            if (!Objects.equals(
                    group.getUser().getUsersId(),
                    currentUser.getUsersId()
            )) {
                throw new GeneralException(
                        GlobalErrorCode.SCHEDULE_NOT_FOUND
                );
            }

            return;
        }

        /*
         * 2. 그룹 없는 일정
         *
         * Schedule → Event → Images → Users
         */
        boolean owner = events.stream()
                .map(Event::getImages)
                .filter(Objects::nonNull)
                .anyMatch(image ->
                        Objects.equals(
                                image.getUser().getUsersId(),
                                currentUser.getUsersId()
                        )
                );

        if (!owner) {
            throw new GeneralException(
                    GlobalErrorCode.SCHEDULE_NOT_FOUND
            );
        }
    }
}