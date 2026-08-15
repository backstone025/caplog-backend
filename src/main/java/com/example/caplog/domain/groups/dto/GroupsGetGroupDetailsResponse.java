package com.example.caplog.domain.groups.dto;

import com.example.caplog.domain.groups.entity.Groups;
import com.example.caplog.domain.groups.type.Category;
import com.example.caplog.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * #8-2 그룹 상세 조회 API 응답
 *
 * @param page          페이지 정보
 * @param group         그룹 정보
 * @param scheduleCount 일정 개수
 * @param schedules     일정 정보 리스트
 */
public record GroupsGetGroupDetailsResponse(
        GroupsPageInfo page,
        GroupInfo group,
        Long scheduleCount,
        List<ScheduleInfo> schedules
) {

    /**
     * 그룹 정보
     *
     * @param groupId       그룹 아이디
     * @param groupName     그룹 제목
     * @param groupCategory 그룹 카테고리
     */
    public record GroupInfo(
            Long groupId,
            String groupName,
            Category groupCategory
    ) {
    }

    /**
     * 일정 정보
     *
     * @param scheduleId 일정 아이디
     * @param title      일정 제목
     * @param imgUrl     이미지 URL
     * @param isNew      최근 업데이트 여부
     */
    public record ScheduleInfo(
            Long scheduleId,
            String title,
            String imgUrl,
            Boolean isNew
    ) {
    }

    // Group, List<Schedule> -> GroupsGetGroupListResponse(DTO)로 변환해 주는 정적 팩토리 메서드
    public static GroupsGetGroupDetailsResponse from(Groups group, Page<Schedule> schedulePage, Map<Long, String> images) {
        // PageInfo
        GroupsPageInfo pageInfo = new GroupsPageInfo(
                schedulePage.getTotalPages(),
                schedulePage.getNumber()
        );

        // GroupInfo
        GroupInfo groupInfo = new GroupInfo(
                group.getGroupId(),
                group.getTitle(),
                group.getCategory()
        );

        // ScheduleInfo
        List<ScheduleInfo> scheduleInfoList = schedulePage.getContent().stream()
                .map(s -> new ScheduleInfo(
                        s.getScheduleId(),
                        s.getTitle(),
                        images.get(s.getScheduleId()),
                        s.checkScheduleRecentlyUpdated()
                ))
                .toList();

        return new GroupsGetGroupDetailsResponse(pageInfo, groupInfo, schedulePage.getTotalElements(), scheduleInfoList);
    }
}
