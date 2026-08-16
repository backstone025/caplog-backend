package com.example.caplog.domain.users.dto;

import com.example.caplog.domain.users.type.ProfileImage;

import java.util.List;
import java.util.Map;

public record UsersProfileImgUrlListResponse(
        List<ImageElement> images
) {
    private record ImageElement(
            ProfileImage profileImg,    // 프로필 이미지 타입
            String imgUrl               // 이미지 URL
    ) {
    }

    public static UsersProfileImgUrlListResponse from(List<ProfileImage> profileImages, Map<String, String> imageMapper) {
        List<ImageElement> images = profileImages.stream()
                .distinct()
                .map(p -> new ImageElement(p, imageMapper.get(p.name())))
                .toList();
        return new UsersProfileImgUrlListResponse(images);
    }
}
