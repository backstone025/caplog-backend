package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.schedule.entity.Schedule;
import com.example.caplog.domain.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Collections;
import java.util.List;

public record AiChatResponse(
        @JsonPropertyDescription("추출된 일정들")
        List<ExtractedSchedule> schedules
) {
    public static List<Schedule> from(List<ExtractedSchedule> schedules, Users user) {
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }
        return schedules.stream().map((s) ->
                Schedule.builder()
                        .user(user)
                        .category(s.parseCategory())
                        .startTime(s.parseStartTime())
                        .endTime(s.parseEndTime())
                        .title(s.title())
                        .description(s.description())
                        .build()).toList();
    }
}
