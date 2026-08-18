package com.example.caplog.domain.schedule.dto.request;

import java.util.List;

public record ScheduleUpdateRequest(
        ScheduleInfo schedule,
        List<EventInfo> events
) {

    public record ScheduleInfo(
            String title,
            String aiSummary,
            String category,
            Boolean hasGroup,
            Long groupId  //false-> null, true -> 연결할 groupId
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
}