package com.cwa.s3crudapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileMetaData {
    private String fileKey;
    private Long size;
    @Builder.Default
    private String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    private LocalDateTime lastModified;
}
