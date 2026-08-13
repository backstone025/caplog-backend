package com.example.caplog.domain.images.controller;

import com.example.caplog.domain.images.entity.Images;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.users.entity.Users;
import com.example.caplog.domain.users.service.UsersService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImagesController {

    private final ImagesService imagesService;
    private final UsersService usersService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> uploadImage(
            @RequestPart("image") MultipartFile image
    ) {

        // 1. 현재 로그인 사용자 조회
        Users user = usersService.getCurrentUser();

        // 2. S3 업로드 + Images DB 저장
        Images savedImage = imagesService.upload(
                image,
                user
        );

        // 3. 저장된 imageId 반환
        return ResponseEntity.ok(
                ApiResponse.success(
                        savedImage.getImageId()
                )
        );
    }
}