package com.example.caplog.domain.ai.chat.dto.request.context;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CaptureContext(
        @JsonPropertyDescription("압축된 캡쳐 키워드들")
        String text
) {
    public static CaptureContext from(List<String> keywords) {
        // 받아온 캡쳐 추출 키워드들 검사 로직
        if (keywords == null || keywords.isEmpty()) {
            return new CaptureContext("(추출된 캡쳐 정보 없음)\n");
        }

        //TODO: 여기 필요에 따라 구현할 것

        return null;
    }
}
