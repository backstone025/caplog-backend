package com.example.caplog.domain.ai.chat.dto.response;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.Collections;
import java.util.List;

public record AiChatResponse(
        @JsonPropertyDescription("추출된 일정들")
        List<ExtractedGroups> schedules
) {
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
