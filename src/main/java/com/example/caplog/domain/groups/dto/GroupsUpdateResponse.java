package com.example.caplog.domain.groups.dto;

import com.example.caplog.domain.groups.type.Category;

public record GroupsUpdateResponse(
        String groupName,
        Category groupcategory
) {
}
