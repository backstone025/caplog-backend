package com.example.caplog.domain.images.dto.request;

import java.util.List;

public record UploadConfirmRequest(
        Long imageId,
        String title,
        String aiSummary,
        String category,

        TargetType targetType,
        Long targetId,

        List<EventRequest> events
) {

    public enum TargetType {
        NONE,
        GROUP,
        SCHEDULE
    }

    public record EventRequest(
            String title,
            String startAt,
            String endAt,
            String location,
            String details
    ) {
    }
}