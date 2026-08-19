package com.jobpilot.resume.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpload {
    private String fileName;
    private String contentType;
    private long size;
    private byte[] bytes;
    private InputStream inputStream;
    private UUID userId;
}
