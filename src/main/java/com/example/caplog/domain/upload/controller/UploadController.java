package com.example.caplog.domain.upload.controller;

import com.example.caplog.domain.upload.dto.ConfirmRequest;
import com.example.caplog.domain.upload.dto.UploadResponse;
import com.example.caplog.domain.upload.service.UploadService;
import com.example.caplog.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<UploadResponse>> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = uploadService.upload(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @PostMapping("/confirm")
//    public ResponseEntity<ApiResponse> confirm(@RequestParam ConfirmRequest confirmRequest) {
//    }


}