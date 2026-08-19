package com.example.caplog.domain.notification.service;

import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.notification.dto.request.NotificationAlarmConsentRequest;
import com.example.caplog.domain.notification.dto.response.NotificationAlarmConsentResponse;
import com.example.caplog.domain.notification.dto.response.NotificationGetAlarmListResponse;
import com.example.caplog.domain.notification.entity.Notification;
import com.example.caplog.domain.notification.exception.NotificationException;
import com.example.caplog.domain.notification.repository.NotificationRepository;
import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final AuthService authService;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final ImagesService imagesService;
    private final UsersService usersService;

    // #9-1 이벤트 알림 목록 조회
    public NotificationGetAlarmListResponse getAlarmList(Integer page, NotificationType type) {
        try {
            Users user = authService.getCurrentUser();
            log.info("[알림 조회 시작] userId: {}, loginId: {}, requestPage: {}, alarmType: {}",
                    user.getUsersId(), user.getLoginId(), page, type);

            int pageSize = 100; // 페이지 크기 : 100개로 설정
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

            Page<Notification> notificationPage;
            if (type == null || type == NotificationType.TOTAL) {
                notificationPage = notificationRepository.findAllByUser(user, pageable);
            } else {
                notificationPage = notificationRepository.findAllByUserAndType(user, type, pageable);
            }

            // 페이지 범위 검사
            this.checkPageRange(notificationPage);

            log.info("[알림 DB 조회 완료] 조회된 알림 건수: {}, 전체 페이지: {}",
                    notificationPage.getContent().size(), notificationPage.getTotalPages());

            List<Notification> notifications = notificationPage.getContent();

            // 1. 조회된 알림들에서 Schedule 목록만 추출
            List<Schedule> scheduleList = notifications.stream()
                    .map(Notification::getSchedule)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            log.info("[Schedule 추출 완료] 연관된 Schedule 건수: {}", scheduleList.size());

            Map<Long, Long> dDayMapper = new HashMap<>();
            Map<Long, String> imagesMapper = new HashMap<>();

            if (!scheduleList.isEmpty()) {
                // 2. Schedule과 관련된 Event 및 대표 Image 일괄 배치 조회
                List<Event> firstEvents = eventRepository.findFirstEventsWithImageByScheduleIn(scheduleList);
                log.info("[Event 배치 조회 완료] 조회된 Event 건수: {}", firstEvents.size());

                Map<Long, Event> scheduleEventMap = firstEvents.stream()
                        .collect(Collectors.toMap(
                                e -> e.getSchedule().getScheduleId(),
                                e -> e,
                                (e1, e2) -> e1
                        ));

                // 3. 알림별 D-Day 및 대표 이미지 URL 추출
                for (Notification n : notifications) {
                    if (n.getSchedule() != null) {
                        Long scheduleId = n.getSchedule().getScheduleId();
                        Event event = scheduleEventMap.get(scheduleId);

                        if (event != null) {
                            // D-Day 계산
                            if (event.getStartAt() != null) {
                                long days = ChronoUnit.DAYS.between(LocalDate.now(), event.getStartAt().toLocalDate());
                                dDayMapper.put(n.getNotificationId(), days);
                            }

                            // 이미지 URL 계산
                            if (event.getImages() != null) {
                                try {
                                    String imgUrl = imagesService.getUrl(event.getImages());
                                    if (imgUrl != null) {
                                        imagesMapper.put(n.getNotificationId(), imgUrl);
                                    }
                                } catch (Exception imgEx) {
                                    log.warn("[이미지 URL 변환 실패] eventId: {}, error: {}", event.getEventId(), imgEx.getMessage());
                                }
                            }
                        }
                    }
                }
            }

            log.info("[알림 목록 응답 생성 완료] dDayMapper size: {}, imagesMapper size: {}",
                    dDayMapper.size(), imagesMapper.size());

            return NotificationGetAlarmListResponse.from(notificationPage, type, dDayMapper, imagesMapper);

        } catch (Exception e) {
            log.error("[알림 목록 조회 실패] 에러 발생: ", e);
            throw e;
        }
    }

    private void checkPageRange(Page<?> notificationPage) {
        int page = notificationPage.getNumber();
        int totalPages = notificationPage.getTotalPages();

        // 경우 1. 음수 페이지 요청 시
        // 경우 2. 데이터가 없을 때 (totalPages == 0) -> page가 0보다 크면 에러 (0 허용)
        // 경우 3. 데이터가 있을 때 (totalPages > 0) -> page가 totalPages 이상이면 에러 (0 ~ totalPages-1 허용)
        if (page < 0 || (totalPages == 0 && page > 0) || (totalPages > 0 && page >= totalPages)) {
            throw new GeneralException(NotificationException.NOTIFICATION_PAGE_BAD_RANGE);
        }
    }

    // #9-2 사용자 알림 동의 확정
    public NotificationAlarmConsentResponse updateAlarmConsent(NotificationAlarmConsentRequest request) {
        UsersDetails usersDetails = usersService.getUsersDetails();

        usersDetails.updateAlarmConsent(request.isApproved());
        return new NotificationAlarmConsentResponse(request.isApproved());
    }

    // #9-3 사용자 알림 동의여부 조회
    public NotificationAlarmConsentResponse getAlarmConsent() {
        UsersDetails usersDetails = usersService.getUsersDetails();
        return new NotificationAlarmConsentResponse(usersDetails.isAlarmConsent());
    }

    // #9-4 사용자 알림 조회 열람 상태 확인으로 설정
    public void markAlarmAsRead(Long alarmId) {
        Notification notification = notificationRepository.findById(alarmId).orElseThrow();
        notification.markAsRead();
    }
}