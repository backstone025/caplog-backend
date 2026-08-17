package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Collections;
import java.util.List;

public record AiChatResponse(
        @JsonPropertyDescription("이미지에서 추출한 분석에 필요한 핵심 텍스트")
        String extractedText,

        @JsonPropertyDescription("이미지 전체를 대표하는 제목")
        String title,

        @JsonPropertyDescription("독립 정보가 없을 때 이미지 전체에 대한 공통 AI 요약")
        String aiSummary,

        @JsonPropertyDescription("독립 정보가 없을 때 공통으로 적용되는 중요 세부사항")
        List<String> details,

        @JsonPropertyDescription("서로 독립적으로 구분되는 정보 목록")
        List<ExtractedItem> items,

        @JsonPropertyDescription("독립 정보가 없을 때 이미지 전체에서 추출된 일정 목록")
        List<ExtractedSchedule> schedules
) {
    public record ExtractedItem(

            @JsonPropertyDescription("독립적으로 구분되는 정보의 제목")
            String title,

            @JsonPropertyDescription("해당 정보에 대한 AI 요약")
            String aiSummary,

            @JsonPropertyDescription("장소, 준비물, 주의사항, 제출물 등 해당 정보의 중요 세부사항")
            List<String> details,

            @JsonPropertyDescription("해당 정보에 포함된 일정 목록. 일정이 없으면 빈 배열")
            List<ExtractedSchedule> schedules
    ) {
    }
    public record ExtractedSchedule(

            @JsonPropertyDescription("일정 제목")
            String title,

            @JsonPropertyDescription("카테고리. 반드시 공부, 학교, 일상, 기타 중 하나")
            String category,

            @JsonPropertyDescription("일정 시작 일시. yyyy-MM-dd HH:mm:ss 형식. 없으면 null")
            String startAt,

            @JsonPropertyDescription("일정 종료 일시. yyyy-MM-dd HH:mm:ss 형식. 없으면 null")
            String endAt,

            @JsonPropertyDescription("일정 장소. 없으면 null")
            String location,

            @JsonPropertyDescription("준비물, 주의사항, 조건 등 해당 일정에 직접 관련된 중요 세부사항")
            List<String> details
    ) {
    }

    public static List<Groups> from(List<ExtractedGroups> groups, Users user) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream()
                .map((g) ->
                        Groups.createGroups(
                                user,
                                g.title(),
                                g.parseCategory()
                        ))
                .toList();
    }
}
