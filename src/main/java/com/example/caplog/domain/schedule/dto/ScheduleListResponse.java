package com.example.caplog.domain.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
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
            String captureImg,
            String title,
            String category
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
            String title
    ) {
    }

    public record EventInfo(
            Long tempId,
            String title,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime dateTime,

            String details
    ) {
    }
}