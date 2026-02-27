package com.gdg.sprint.team1.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.gdg.sprint.team1.exception.FileUploadException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/png",
        "image/jpeg",
        "image/webp"
    );

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.info("파일이 제공되지 않아 null 반환");
            return null;
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank() || originalFilename.equals("blob")) {
            log.info("유효하지 않은 파일명({}), null 반환", originalFilename);
            return null;
        }

        if (file.getSize() == 0) {
            log.info("파일 크기가 0, null 반환");
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            String errorMsg = String.format(
                "파일 크기가 너무 큽니다. (최대: 10MB, 현재: %.2fMB)",
                file.getSize() / (1024.0 * 1024.0)
            );
            log.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String contentType = file.getContentType();
        if (contentType == null ||
            !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            String errorMsg = "이미지 파일만 업로드 가능합니다. (지원: PNG, JPG, WEBP)";
            log.warn("지원하지 않는 파일 형식: {}", contentType);
            throw new IllegalArgumentException(errorMsg);
        }

        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
        log.info("S3 업로드 시작: 원본={}, 고유명={}, 크기={}bytes",
            originalFilename, uniqueFilename, file.getSize());

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFilename)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, file.getSize())
                );
            }

            String fileUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                uniqueFilename
            );
            log.info("S3 업로드 성공: URL={}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", originalFilename, e);
            throw new FileUploadException("파일 업로드에 실패했습니다.", e);

        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new FileUploadException("S3 업로드에 실패했습니다.", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.debug("삭제할 파일 URL이 없음");
            return;
        }

        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        log.info("S3 삭제 시작: 파일명={}", filename);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 삭제 성공: 파일명={}", filename);
        } catch (Exception e) {
            log.error("S3 삭제 실패: 파일명={}, 에러={}", filename, e.getMessage(), e);
        }
    }
}