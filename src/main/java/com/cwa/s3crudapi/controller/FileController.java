package com.cwa.s3crudapi.controller;

import com.cwa.s3crudapi.dto.FileMetaData;
import com.cwa.s3crudapi.dto.FileUploadResponse;
import com.cwa.s3crudapi.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@Tag(name = "File Management", description = "APIs for managing files in S3")
public class FileController {

    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload a file to S3", description = "Uploads a file to the specified S3 bucket")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "The file to be uploaded", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            final var fileName = s3Service.uploadFile(file);
            final var fileUrl = s3Service.getPresignedUrl(fileName);

            final var response = new FileUploadResponse(fileName, fileUrl, "File uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileUploadResponse(null, null, e.getMessage()));
        }
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Download a file from S3", description = "Downloads a file from the specified S3 bucket")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @Parameter(description = "The name of the file to be downloaded", required = true)
            @PathVariable String fileName) {
        final var file = s3Service.downloadFile(fileName);
        final var resource = new ByteArrayResource(file);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .contentLength(file.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/presigned-url/{fileName}")
    @Operation(summary = "Get a presigned URL for a file", description = "Generates a presigned URL for downloading a file from S3")
    public ResponseEntity<String> getPresignedUrl(
            @Parameter(description = "The name of the file to generate a presigned URL for", required = true)
            @PathVariable String fileName) {
        final var presignedUrl = s3Service.getPresignedUrl(fileName);
        return ResponseEntity.ok(presignedUrl);
    }

    @GetMapping("/list")
    @Operation(summary = "List files in S3 bucket", description = "Lists all files in the specified S3 bucket")
    public ResponseEntity<List<FileMetaData>> listFiles() {
        final var files = s3Service.listFiles();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/delete/{fileName}")
    @Operation(summary = "Delete a file from S3 bucket", description = "Deletes a file from the specified S3 bucket")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "The name of the file to be deleted", required = true)
            @PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully: " + fileName);
    }
}
