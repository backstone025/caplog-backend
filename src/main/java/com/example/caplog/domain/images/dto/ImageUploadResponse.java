package com.example.caplog.domain.images.dto;

import com.example.caplog.domain.ai.chat.dto.response.AiChatResponse;
import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.type.ImageStatus;

public record ImageUploadResponse(
        Long imageId,
        ImageStatus imageStatus,
        AiChatResponse analysis
) {

    public static ImageUploadResponse from(Images image, AiChatResponse analysis) {
        return new ImageUploadResponse(
                image.getImageId(),
                image.getImageStatus(),
                analysis
        );
    }
}