package com.example.caplog.domain.images.dto;

import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.type.ImageStatus;

public record ImageUploadResponse(
        Long imageId,
        ImageStatus imageStatus
) {

    public static ImageUploadResponse from(Images image) {
        return new ImageUploadResponse(
                image.getImageId(),
                image.getImageStatus()
        );
    }
}