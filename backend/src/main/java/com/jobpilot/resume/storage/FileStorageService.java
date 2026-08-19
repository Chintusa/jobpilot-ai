package com.jobpilot.resume.storage;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface FileStorageService {

    StoredFile store(FileUpload file);

    void delete(String fileId);

    StoredFile get(String fileId);

    // Backward-compatible utility methods for MultipartFile
    default String storeFile(MultipartFile file, UUID userId) {
        try {
            FileUpload upload = FileUpload.builder()
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .bytes(file.getBytes())
                    .inputStream(file.getInputStream())
                    .userId(userId)
                    .build();
            return store(upload).getFileUrl();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    default byte[] loadFile(String fileUrl) {
        StoredFile stored = get(fileUrl);
        return stored != null ? stored.getContent() : new byte[0];
    }

    default void deleteFile(String fileUrl) {
        delete(fileUrl);
    }
}
