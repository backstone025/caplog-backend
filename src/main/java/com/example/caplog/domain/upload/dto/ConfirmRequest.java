package com.example.caplog.domain.upload.dto;

import com.example.caplog.domain.groups.type.Category;

import java.util.List;

public record ConfirmRequest(
        Long imageId,
        String title,
        Category category,
        String scheduleAiSummary,
        String group,
        Long groupId,
        List<EventConfirmDto> events
) {
    public record EventConfirmDto(
            boolean isChecked,   // 체크여부
            String title,
            String details,
            String aiSummary,
            String videoUrl,
            String date,
            String startAt,
            String endAt
    ) {}

    // VectorDB 결과 매칭용 copy 생성자
    public ConfirmRequest withGroupInfo(Long groupId, String groupName) {
        return new ConfirmRequest(
                this.imageId,
                this.title,
                this.category,
                this.scheduleAiSummary,
                groupName,
                groupId,
                this.events
        );
    }
}