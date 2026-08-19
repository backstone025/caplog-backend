package com.example.caplog.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleUpdateResponse(

        ScheduleInfo schedule,
        List<EventInfo> events

) {

    public record ScheduleInfo(
            Long scheduleId,
            String title,
            String aiSummary,
            String category,
            boolean hasGroup,
            Long groupId,
            String groupTitle
    ) {
    }

    public record EventInfo(
            Long id,
            String title,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startAt,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endAt,

            String details
    ) {
    }
}