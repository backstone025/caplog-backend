package com.example.caplog.domain.groups.controller;

import com.example.caplog.domain.groups.dto.GroupsGetGroupListResponse;
import com.example.caplog.domain.groups.dto.GroupsGetCategoriesResponse;
import com.example.caplog.domain.groups.dto.GroupsUpdateRequest;
import com.example.caplog.domain.groups.dto.GroupsUpdateResponse;
import com.example.caplog.domain.groups.service.GroupsService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupsController {
    private final GroupsService groupsService;

    // #4 그룹/단일 일정 전체 조회 API
    @GetMapping
    public ResponseEntity<ApiResponse<GroupsGetGroupListResponse>>  getGroups(
            @RequestParam Integer page
    ) {
        GroupsGetGroupListResponse response = groupsService.getGroups(page);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #5 그룹 카테고리 목록 조회 API
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<GroupsGetCategoriesResponse>> getCategories() {
        GroupsGetCategoriesResponse response = groupsService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // #6 그룹 수정 API
    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupsUpdateResponse>> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupsUpdateRequest request) {
        GroupsUpdateResponse response = groupsService.updateGroups(groupId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
