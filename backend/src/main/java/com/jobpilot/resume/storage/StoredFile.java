package com.jobpilot.resume.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private long size;
    private byte[] content;
    private Instant createdAt;
}
