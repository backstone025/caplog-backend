package com.example.caplog.domain.notification.service;

import com.example.caplog.domain.ai.alarm.AiAlarmService;
import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.notification.entity.Notification;
import com.example.caplog.domain.notification.repository.NotificationRepository;
import com.example.caplog.domain.notification.type.NotificationType;
import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {
    private final UsersService usersService;
    private final EventRepository eventRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final VectorService vectorService;
    private final AiAlarmService aiAlarmService;
    private final FcmService fcmService;

    // 매일 오전 9시마다 실행되는 통합 알림 생성(batch)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void generateNotifications() {
        log.info("[알림 배치 스케줄러] 알림 생성 작업을 시작합니다.");

        List<UsersDetails> usersDetails = usersService.getAllUsersDetails();
        Map<Long, Users> usersMap = usersService.getUsersMap();

        log.info("[알림 배치 스케줄러] 총 {}명의 사용자 설정을 조회했습니다.", usersDetails.size());

        int processedUserCount = 0;

        for (UsersDetails usersDetail : usersDetails) {
            Users user = usersMap.get(usersDetail.getUserId());
            // 사용자 조회 불가 & 알림 수신 동의 꺼져있을 경우
            if (user == null || !usersDetail.isAlarmConsent()) {
                log.debug("[알림 배치 스케줄러] Skip - 사용자 없음 또는 수신동의 OFF (userId: {})", usersDetail.getUserId());
                continue;
            }

            processedUserCount++;
            log.info("[알림 배치 스케줄러] 사용자 처리 시작 (userId: {}, loginId: {})", user.getUsersId(), user.getLoginId());

            // 1. 임박한 알림 생성
            if (usersDetail.isImminentAlarm()) {
                List<Notification> imminentNotifications = createImminentNotifications(user);
                notificationRepository.saveAll(imminentNotifications);
                imminentNotifications.forEach(n-> {
                    sendMessageToUser(NotificationType.IMMINENT, usersDetail, n.getTitle(), n.getContent());
                });

                log.info("[IMMINENT 알림] 저장 완료 (userId: {}, 건수: {})", user.getUsersId(), imminentNotifications.size());
            }

            // 2. 미확인 알림 생성
            if (usersDetail.isUnviewedAlarm()) {
                List<Notification> unviewedNotifications = createUnviewedNotifications(user);
                notificationRepository.saveAll(unviewedNotifications);
                unviewedNotifications.forEach(n -> {
                    sendMessageToUser(NotificationType.UNVIEWED, usersDetail, n.getTitle(), n.getContent());
                });
                log.info("[UNVIEWED 알림] 저장 완료 (userId: {}, 건수: {})", user.getUsersId(), unviewedNotifications.size());
            }

            // 3. AI 추천 알림 생성
            if (usersDetail.isAiRecommendedAlarm()) {
                Notification aiRecommendedNotification = createAiRecommendedNotifications(user);
                if(aiRecommendedNotification != null){
                    notificationRepository.save(aiRecommendedNotification);
                    sendMessageToUser(
                            NotificationType.AI_RECOMMENDED,
                            usersDetail,
                            aiRecommendedNotification.getTitle(),
                            aiRecommendedNotification.getContent());
                    log.info("[AI_RECOMMENDED 알림] 저장 완료 (userId: {})", user.getUsersId());
                }

            }
        }

        log.info("[알림 배치 스케줄러] 알림 생성 작업 종료 (대상 사용자: {}/{}명)", processedUserCount, usersDetails.size());
    }

    // 1. IMMINENT (임박한 알림) 생성 로직
    private List<Notification> createImminentNotifications(Users user) {
        LocalDateTime startDay = LocalDateTime.now();
        LocalDateTime endDay = startDay.plusDays(3);    // 3일 간격으로 조회

        List<Event> imminentEvents = eventRepository.findImminentEventsByUsersBetweenStartAndEndDay(
                user,
                startDay,
                endDay
        );

        List<Notification> imminentNotifications = new ArrayList<>();

        for (Event event : imminentEvents) {
            // Event로부터 Schedule 추출
            Schedule schedule = event.getSchedule();
            // 남은 일수 계산
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), event.getStartAt().toLocalDate());

            String title = "얼마 남지 않는 일정";
            String content;
            if (daysLeft == 0) {
                content = String.format("'%s'이 오늘 예정되어 있어요!", event.getTitle());
            } else {
                content = String.format("'%s'이 %d일 남았어요!", event.getTitle(), daysLeft);
            }

            Notification notification = Notification.createNotification(
                    user,
                    schedule,
                    NotificationType.IMMINENT,
                    title,
                    content,
                    false
            );
            imminentNotifications.add(notification);
        }
        return imminentNotifications;
    }

    // 2. UNVIEWED (미확인 알림) 생성 로직
    private List<Notification> createUnviewedNotifications(Users user) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(7); // 1주일 동안 확인하지 않는 일정에 대한 탐색 범위 설정

        List<Schedule> unviewedSchedules = scheduleRepository.findUnviewedSchedulesByUser(
                user,
                thresholdDate,
                PageRequest.of(0, 5)        // 최대 5개 가져오도록 설정
        );

        List<Notification> unviewedNotifications = new ArrayList<>();

        for (Schedule schedule : unviewedSchedules) {
            String title = "한 번도 열람하지 않는 정보";
            String content = String.format("저장한 '%s' 아직 확인하지 않았어요.", schedule.getTitle());

            Notification notification = Notification.createNotification(
                    user,
                    schedule,
                    NotificationType.UNVIEWED,
                    title,
                    content,
                    false
            );
            unviewedNotifications.add(notification);
        }

        return unviewedNotifications;
    }

    // 3. AI_RECOMMENDED (AI 추천 알림) 생성 로직
    private Notification createAiRecommendedNotifications(Users user) {
        String randomKeyword = aiAlarmService.getRandomKeyword();
        List<Document> userGroups = vectorService.searchGroupsVector(user.getUsersId(), randomKeyword);

        if (userGroups.isEmpty()) {
            return null;
        }

        for (Document userGroup : userGroups) {
            Long groupId = aiAlarmService.extractGroupIdFromDocument(userGroup);
            if (groupId == null) {
                continue;
            }

            List<Schedule> schedules = scheduleRepository.findByGroupsGroupId(groupId);
            if (schedules != null && !schedules.isEmpty()) {
                int randomIndex = ThreadLocalRandom.current().nextInt(0, schedules.size());
                Schedule schedule = schedules.get(randomIndex);

                String aiComment = aiAlarmService.generateRecommendationMessage(schedule);
                String title = "AI 추천";
                String content = String.format("%s", aiComment);

                return Notification.createNotification(
                        user,
                        schedule,
                        NotificationType.AI_RECOMMENDED,
                        title,
                        content,
                        false
                );
            }
        }
        return null;
    }

    // FCM 발송
    private void sendMessageToUser(NotificationType notificationType, UsersDetails usersDetails, String title, String content) {
        log.debug("[FCM 발송 시도] type: {}, targetUserId: {}", notificationType, usersDetails.getUserId());

        String fcmToken = usersDetails.getFcmToken();

        if (fcmToken != null && !fcmToken.isBlank()) {
            fcmService.sendMessageTo(fcmToken, title, content);
        }else {
            log.warn("[FCM 스킵] FCM 토큰이 없습니다 - userId: {}", usersDetails.getUserId());
        }
    }
}