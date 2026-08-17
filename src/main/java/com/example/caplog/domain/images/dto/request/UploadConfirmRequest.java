package com.example.caplog.domain.images.dto.request;

import java.util.List;

public record UploadConfirmRequest(
        Long imageId,
        String title,
        String aiSummary,
        String category,
        Long groupId,
        List<EventRequest> events
) {

    public record EventRequest(
            String title,
            String startAt,
            String endAt,
            String location,
            String details
    ) {
    }
}