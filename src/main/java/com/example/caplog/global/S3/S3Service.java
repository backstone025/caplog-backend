package com.example.caplog.global.S3;

import com.example.caplog.global.error.code.GlobalErrorCode;
import com.example.caplog.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, Long userId) {

        System.out.println("=== S3 upload 시작 ===");

        validateFile(file);
        System.out.println("=== 파일 검증 통과 ===");

        String key = createKey(file, userId);
        System.out.println("=== 생성된 key: " + key + " ===");

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            System.out.println("=== S3 putObject 호출 직전 ===");

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            System.out.println("=== S3 업로드 성공 ===");

            return key;

        } catch (Exception e) {
            System.out.println("=== S3 업로드 실패 ===");
            System.out.println(e.getMessage());
            e.printStackTrace();

            throw new GeneralException(
                    GlobalErrorCode.S3_UPLOAD_FAILED
            );
        }
    }

    public void delete(String key) {

        if (key == null || key.isBlank()) {
            throw new GeneralException(
                    GlobalErrorCode.INVALID_INPUT_VALUE
            );
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (S3Exception e) {
            throw new GeneralException(
                    GlobalErrorCode.S3_DELETE_FAILED
            );
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new GeneralException(
                    GlobalErrorCode.INVALID_INPUT_VALUE
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new GeneralException(
                    GlobalErrorCode.INVALID_INPUT_VALUE
            );
        }
    }

    private String createKey(
            MultipartFile file,
            Long userId
    ) {

        String originalFilename =
                file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            );
        }

        return "users/"
                + userId
                + "/"
                + UUID.randomUUID()
                + extension;
    }
}