package com.example.caplog.domain.images.controller;


import com.example.caplog.domain.auth.service.AuthService;
import com.example.caplog.domain.images.dto.ImageUploadResponse;
import com.example.caplog.domain.images.dto.request.UploadConfirmRequest;
import com.example.caplog.domain.images.service.ImagesService;
import com.example.caplog.domain.users.entity.Users;
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
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestPart("image") MultipartFile image
    ) {

        Users user = authService.getCurrentUser();

        ImageUploadResponse result =
                imagesService.upload(image, user);

        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Long>> confirmUpload(
            @RequestBody UploadConfirmRequest request
    ) {

        Long scheduleId =
                imagesService.confirmUpload(request);

        return ResponseEntity.ok(
                ApiResponse.success(scheduleId)
        );
    }
}