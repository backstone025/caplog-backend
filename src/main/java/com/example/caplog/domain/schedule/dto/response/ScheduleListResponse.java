package com.example.caplog.domain.schedule.dto.response;

import java.util.List;

public record ScheduleListResponse(
        PageInfo page,
        List<ListItem> list
) {

    public record PageInfo(
            int totalPage,
            int pageNumber
    ) {
    }

    public record ListItem(
            boolean isGroup,
            Long id,
            boolean isNew,
            int elementCount,
            List<PictureInfo> pictures,
            ScheduleInfo schedule,
            List<EventInfo> events
    ) {
    }

    public record PictureInfo(
            String captureImg
    ) {
    }

    public record ScheduleInfo(
            String title,
            String aiSummary,
            boolean hasGroup,
            GroupInfo group
    ) {
    }

    public record GroupInfo(
            Long groupId,
            String groupName
    ) {
    }

    public record EventInfo(
            Long tempId,
            String title,
            String dateTime,
            String details
    ) {
    }
}