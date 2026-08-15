package com.example.caplog.domain.groups.controller;

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

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupsUpdateResponse>> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupsUpdateRequest request) {
        GroupsUpdateResponse response = groupsService.updateGroups(groupId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
