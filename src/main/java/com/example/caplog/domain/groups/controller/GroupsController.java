package com.example.caplog.domain.groups.controller;

import com.example.caplog.domain.groups.dto.request.GroupsUpdateRequest;
import com.example.caplog.domain.groups.dto.response.GroupsGetCategoriesResponse;
import com.example.caplog.domain.groups.dto.response.GroupsGetGroupDetailsResponse;
import com.example.caplog.domain.groups.dto.response.GroupsGetGroupListResponse;
import com.example.caplog.domain.groups.dto.response.GroupsUpdateResponse;
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

    // #7 그룹 삭제 API
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId
    ){
        groupsService.deleteGroups(groupId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // #8-1 그룹 상세 조회
    @GetMapping("/details/{groupId}")
    public ResponseEntity<ApiResponse<GroupsGetGroupDetailsResponse>> getGroupDetails(
            @PathVariable Long groupId,
            @RequestParam Integer page
    ){
        GroupsGetGroupDetailsResponse response = groupsService.getGroupDetails(groupId, page);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
