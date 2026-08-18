package com.example.caplog.domain.groups.dto.response;

import com.example.caplog.domain.groups.dto.GroupsPageInfo;
import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record GroupsGetGroupListResponse(
        GroupsPageInfo page,
        List<GroupElement> groupList
) {

    public record GroupElement(
            Long groupId,         // 그룹/단일 일정 그룹 아이디
            String groupName,     // 그룹/단일 일정 그룹 이름
            String targetType     // 그룹/단일 일정인지 여부 구분
    ) {
    }

    // List<Groups> -> GroupsGetGroupListResponse(DTO)로 변환해 주는 정적 팩토리 메서드
    public static GroupsGetGroupListResponse from(Page<Groups> groupsPage, Page<Schedule> schedulePage) {
        // 두 도메인 중 더 큰 totalPages 계산
        int maxTotalPages = Math.max(groupsPage.getTotalPages(), schedulePage.getTotalPages());

        // 데이터가 아예 없는 경우 최소 1페이지 보장 (또는 0)
        GroupsPageInfo groupsPageInfo = new GroupsPageInfo(
                maxTotalPages,
                groupsPage.getNumber()
        );

        List<GroupElement> groupElements = new ArrayList<>();

        if (groupsPage.getNumber() == 0) {
            groupElements.add(new GroupElement(null, "주제 없음", "NONE"));
        }

        List<GroupElement> groupElements2 = groupsPage.getContent().stream()
                .map(group -> new GroupElement(group.getGroupId(), group.getTitle(), "GROUP"))
                .toList();

        List<GroupElement> groupElements1 = schedulePage.getContent().stream()
                .map(s -> new GroupElement(null, s.getTitle(), "SCHEDULE"))
                .toList();

        groupElements.addAll(groupElements1);
        groupElements.addAll(groupElements2);

        return new GroupsGetGroupListResponse(groupsPageInfo, groupElements);
    }
}
