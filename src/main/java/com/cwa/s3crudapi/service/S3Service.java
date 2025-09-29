package com.cwa.s3crudapi.service;

import com.cwa.s3crudapi.dto.FileMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        log.info("Uploading file to S3 bucket: {}", bucketName);
        final var fileName = generateFileName(file);

        final var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        log.info("File uploaded successfully to S3 bucket: {}", bucketName);

        return fileName;
    }

    public byte[] downloadFile(String fileName) {
        log.info("Downloading file from S3 bucket: {}", bucketName);
        final var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    }

    public String getPresignedUrl(String fileName) {
        log.info("Generating presigned URL for file: {}", fileName);
        final var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(gor -> gor.bucket(bucketName).key(fileName))
                .build();

        final var getObjectPresignedRequest = s3Presigner.presignGetObject(presignRequest);
        return getObjectPresignedRequest.url().toString();
    }

    public List<FileMetaData> listFiles() {
        log.info("Listing files in S3 bucket: {}", bucketName);
        final var listRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .build();

        final var listResponse = s3Client.listObjects(listRequest);
        return listResponse.contents().stream()
                .map(s3Object -> FileMetaData.builder()
                        .fileKey(s3Object.key())
                        .size(s3Object.size())
                        .lastModified(getLocalDateTime(s3Object.lastModified().toString()))
                        .build())
                .toList();
    }

    public void deleteFile(String fileName) {
        log.info("Deleting file with name: {}", fileName);
        final var deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("File deleted successfully from S3 bucket: {}", bucketName);
    }

    private String generateFileName(final MultipartFile file) {
        return String.format("%s-%s", UUID.randomUUID(), file.getOriginalFilename());
    }

    private LocalDateTime getLocalDateTime(final String dateString) {
        if (Objects.isNull(dateString) || dateString.isBlank()) return null;
        final var instant = Instant.parse(dateString);

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
