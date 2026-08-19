package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.groups.type.Category;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ExtractedGroups(
        @JsonPropertyDescription("일정 제목")
        String title,

        @JsonPropertyDescription("일정 카테고리 (반드시 다음 중 하나로 지정: DEFAULT")
        String category
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
