package com.example.caplog.domain.notification.service;

import com.example.caplog.domain.ai.alarm.AiAlarmService;
import com.example.caplog.domain.ai.vector.VectorService;
import com.example.caplog.domain.event.repository.EventRepository;
import com.example.caplog.domain.notification.repository.NotificationRepository;
import com.example.caplog.domain.schedule.repository.ScheduleRepository;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.entity.UsersDetails;
import com.example.caplog.domain.users.service.UsersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerServiceTest {

    @InjectMocks
    private NotificationSchedulerService notificationSchedulerService;

    @Mock private UsersService usersService;
    @Mock private EventRepository eventRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private VectorService vectorService;
    @Mock private AiAlarmService aiAlarmService;

    @Test
    @DisplayName("알림 수신동의가 꺼진 사용자는 알림을 생성하지 않는다")
    void generateNotifications_alarmConsentFalse() {
        // given
        // 1. 정적 팩토리 메서드로 생성 후 Reflection으로 usersId 설정
        Users user = Users.createUsers("testUser", "password123");
        ReflectionTestUtils.setField(user, "usersId", 1L);

        // 2. UsersDetails 가짜 데이터 생성 (동일하게 Reflection 활용 또는 Mock 가능)
        UsersDetails details = UsersDetails.createUsersDetails(user);
        ReflectionTestUtils.setField(details, "userId", 1L);
        ReflectionTestUtils.setField(details, "alarmConsent", false); // 수신 거부

        given(usersService.getAllUsersDetails()).willReturn(List.of(details));
        given(usersService.getUsersMap()).willReturn(Map.of(1L, user));

        // when
        notificationSchedulerService.generateNotifications();

        // then
        verify(eventRepository, never()).findImminentEventsByUsersBetweenStartAndEndDay(any(), any(), any());
        verify(notificationRepository, never()).saveAll(anyList());
    }
}