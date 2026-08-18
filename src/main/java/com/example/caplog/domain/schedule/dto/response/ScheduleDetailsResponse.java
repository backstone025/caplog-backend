package com.example.caplog.domain.schedule.dto.response;

import com.example.caplog.domain.event.entity.Event;
import com.example.caplog.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record ScheduleDetailsResponse(

        Long scheduleId,

        List<String> imgUrl,

        String title,

        String aiSummary,

        int eventCount,

        List<EventInfo> events

) {

    public record EventInfo(

            Long id,

            String title,

            boolean hasDate,

            String startAt,

            String endAt,

            String location,

            String details
    ) {
    }

    public static ScheduleDetailsResponse from(
            Schedule schedule,
            List<Event> events,
            List<String> imageUrls
    ) {

        List<EventInfo> eventInfos = events.stream()
                .map(event -> new EventInfo(
                        event.getEventId(),
                        event.getTitle(),
                        event.getStartAt() != null
                                || event.getEndAt() != null,
                        formatDateTime(event.getStartAt()),
                        formatDateTime(event.getEndAt()),
                        event.getLocation(),
                        event.getDetails()
                ))
                .toList();

        return new ScheduleDetailsResponse(
                schedule.getScheduleId(),
                imageUrls,
                schedule.getTitle(),
                schedule.getAiSummary(),
                eventInfos.size(),
                eventInfos
        );
    }

    private static String formatDateTime(
            LocalDateTime dateTime
    ) {

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
