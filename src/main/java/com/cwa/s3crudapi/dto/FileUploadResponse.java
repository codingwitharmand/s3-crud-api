package com.cwa.s3crudapi.dto;

public record FileUploadResponse(
        String fileName,
        String fileUrl,
        String message
) {
}
