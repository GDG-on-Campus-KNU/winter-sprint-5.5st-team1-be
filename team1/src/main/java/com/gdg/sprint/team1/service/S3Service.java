package com.gdg.sprint.team1.service;

import java.io.IOException;
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

        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
        log.info("S3 업로드 시작: 원본={}, 고유명={}", originalFilename, uniqueFilename);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFilename)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(file.getBytes())
            );

            String fileUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                uniqueFilename
            );
            log.info("S3 업로드 성공: URL={}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", originalFilename, e);
            throw new FileUploadException("파일 업로드에 실패했습니다.", e);

        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드에 실패했습니다.", e);
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