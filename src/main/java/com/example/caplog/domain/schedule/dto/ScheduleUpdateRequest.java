package com.example.caplog.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ScheduleUpdateRequest(

        ScheduleInfo schedule,
        List<EventInfo> events

) {

    public record ScheduleInfo(
            String title,
            String aiSummary,
            String category,
            Long groupId
    ) {
    }

    public record EventInfo(
            Long id,
            String title,


            LocalDateTime startAt,


            LocalDateTime endAt,

            String details
    ) {
    }
}