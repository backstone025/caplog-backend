package com.example.caplog.domain.schedule.dto.response;

import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record ScheduleUpdateResponse(
        ScheduleInfo schedule,
        List<EventInfo> events
) {

    public record ScheduleInfo(
            String title,
            String aiSummary,
            String category,
            boolean hasGroup,
            Long groupId
    ) {
    }

    public record EventInfo(
            Long id,
            String title,
            String startAt,
            String endAt,
            String location,
            String details
    ) {
    }

    public static ScheduleUpdateResponse from(
            Schedule schedule,
            List<Event> events
    ) {

        ScheduleInfo scheduleInfo =
                new ScheduleInfo(
                        schedule.getTitle(),
                        schedule.getAiSummary(),
                        schedule.getCategory().name(),
                        schedule.getGroups() != null,
                        schedule.getGroups() != null
                                ? schedule.getGroups().getGroupId()
                                : null
                );

        List<EventInfo> eventInfos =
                events.stream()
                        .map(event ->
                                new EventInfo(
                                        event.getEventId(),
                                        event.getTitle(),
                                        format(event.getStartAt()),
                                        format(event.getEndAt()),
                                        event.getLocation(),
                                        event.getDetails()
                                )
                        )
                        .toList();

        return new ScheduleUpdateResponse(
                scheduleInfo,
                eventInfos
        );
    }

    private static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.format(
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"
                )
        );
    }
}