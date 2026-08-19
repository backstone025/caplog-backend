package com.example.caplog.domain.groups.dto.request;

import com.example.caplog.domain.groups.type.Category;

public record GroupsUpdateRequest(
        String groupName,
        String category
) {
    public Category getCategoryEnum() {
        return Category.valueOf(category);
    }
}
