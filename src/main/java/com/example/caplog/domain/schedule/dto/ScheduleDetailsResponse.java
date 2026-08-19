package com.example.caplog.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
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

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startAt,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endAt,

            String details
    ) {
    }
}