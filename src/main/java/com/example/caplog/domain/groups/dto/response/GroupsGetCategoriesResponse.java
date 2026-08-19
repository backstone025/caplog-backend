package com.example.caplog.domain.groups.dto.response;

import com.example.caplog.domain.groups.type.Category;

import java.util.List;

public record GroupsGetCategoriesResponse(
        List<String> categories
) {
}
