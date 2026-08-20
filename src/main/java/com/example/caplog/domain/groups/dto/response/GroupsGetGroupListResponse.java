package com.example.caplog.domain.groups.dto.response;

import com.example.caplog.domain.groups.dto.GroupsPageInfo;
import com.example.caplog.domain.groups.entity.Groups;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record GroupsGetGroupListResponse(
        GroupsPageInfo page,
        List<GroupElement> groupList
) {

    public record GroupElement(
            Long groupId,         // 그룹/단일 일정 그룹 아이디
            String groupName      // 그룹/단일 일정 그룹 이름
    ) {
    }

    public static GroupsGetGroupListResponse from(Page<Groups> groupsPage) {
        GroupsPageInfo groupsPageInfo = new GroupsPageInfo(
                groupsPage.getTotalPages(),
                groupsPage.getNumber()
        );

        List<GroupElement> groupElements = new ArrayList<>(
                groupsPage.getContent().stream()
                        .map(group -> new GroupElement(group.getGroupId(), group.getTitle()))
                        .toList()
        );

        // 맨 앞(인덱스 0)에 "선택 안함" 추가
        groupElements.add(0, new GroupElement(null, "선택 안함"));

        return new GroupsGetGroupListResponse(groupsPageInfo, groupElements);
    }
}