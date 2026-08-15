package com.example.caplog.domain.groups.dto;

import com.example.caplog.domain.groups.type.Category;

import java.util.List;

public record GroupsGetCategoriesResponse(
        List<Category> categories
) {
}
