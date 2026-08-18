package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.groups.type.Category;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExtractedGroups(
        @JsonPropertyDescription("일정 제목")
        String title,

        @JsonPropertyDescription("카테고리. 반드시 STUDY, SCHOOL, DAILY, ETC 하나")
        String category,

        @JsonPropertyDescription("일정 시작 일시. yyyy-MM-dd HH:mm:ss 형식. 일정 정보가 없으면 null")
        String startAt,

        @JsonPropertyDescription("일정 장소. 장소가 없으면 null")
        String location,

        @JsonPropertyDescription("준비물, 주의사항, 제출물 등 일정 수행에 필요한 핵심 추가 정보")
        String details
) {

    public Category parseCategory() {
        if (category == null || category.isBlank()) {
            return Category.ETC;
        }
        try {
            return Category.valueOf(this.category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Category.ETC;
        }
    }
}
