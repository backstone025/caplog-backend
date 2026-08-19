package com.example.caplog.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ScheduleEventRangeResponse(

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startDateTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endDateTime,

        List<DateCount> dateCounts,

        List<EventInfo> events
) {

    public record DateCount(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            long count
    ) {
    }

    public record EventInfo(
            Long scheduleId,
            Long eventId,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime date,

            String captureImg,
            String eventTitle
    ) {
    }
}