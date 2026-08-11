package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.schedule.type.Category;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ExtractedSchedule(
        @JsonPropertyDescription("일정 제목")
        String title,

        @JsonPropertyDescription("일정 카테고리 (반드시 다음 중 하나로 지정: DEFAULT")
        String category,

        @JsonPropertyDescription("시작 일시 (반드시 'yyyy-MM-dd HH:mm:ss' 형식의 문자열로 응답)")
        String startTime,

        @JsonPropertyDescription("종료 일시 (반드시 'yyyy-MM-dd HH:mm:ss' 형식의 문자열로 응답)")
        String endTime,

        @JsonPropertyDescription("일정 상세 설명")
        String description
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LocalDateTime parseStartTime() {
        if (startTime == null || startTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(this.startTime, FORMATTER);
    }

    public LocalDateTime parseEndTime() {
        if (endTime == null || endTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(this.endTime, FORMATTER);
    }

    public Category parseCategory() {
        if (category == null || category.isBlank()) {
            return Category.DEFAULT;
        }
        try {
            return Category.valueOf(this.category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Category.DEFAULT;
        }
    }
}
