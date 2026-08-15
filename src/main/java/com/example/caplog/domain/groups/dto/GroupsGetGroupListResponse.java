package com.example.caplog.domain.groups.dto;

import com.example.caplog.domain.groups.entity.Groups;
import org.springframework.data.domain.Page;

import java.util.List;

public record GroupsGetGroupListResponse(
        GroupsPageInfo page,
        List<GroupElement> groupList
) {

    public record GroupElement(
            Long groupId,         // 그룹/단일 일정 그룹 아이디
            String groupName     // 그룹/단일 일정 그룹 이름
    ) {
    }

    // List<Groups> -> GroupsGetGroupListResponse(DTO)로 변환해 주는 정적 팩토리 메서드
    public static GroupsGetGroupListResponse from(Page<Groups> groupsPage) {
        // Page 생성
        GroupsPageInfo groupsPageInfo = new GroupsPageInfo(
                groupsPage.getTotalPages(),
                groupsPage.getNumber()
        );

        // Entity List -> DTO List 변환
        List<GroupElement> groupElements = groupsPage.getContent().stream()
                .map(group -> new GroupElement(group.getGroupId(), group.getTitle()))
                .toList();

        return new GroupsGetGroupListResponse(groupsPageInfo, groupElements);
    }
}
