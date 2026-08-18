package com.example.caplog.domain.schedule.dto.response;

public record ScheduleDeleteResponse(
        String userName
) {
    public static ScheduleDeleteResponse success() {
        return new ScheduleDeleteResponse(null);
    }
}