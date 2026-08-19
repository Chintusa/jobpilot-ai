package com.jobpilot.resume.storage;

import com.jobpilot.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements FileStorageService {

    private final Path storageDirectory;

    public LocalStorageService() {
        this.storageDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "jobpilot-storage", "resumes");
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            log.error("Could not create local resume storage directory: {}", e.getMessage());
        }
    }

    @Override
    public StoredFile store(FileUpload file) {
        String originalFilename = file.getFileName();
        String safeName = (originalFilename != null ? originalFilename : "resume.pdf").replaceAll("[^a-zA-Z0-9._-]", "_");
        UUID userId = file.getUserId() != null ? file.getUserId() : UUID.randomUUID();
        String fileId = userId + "_" + System.currentTimeMillis() + "_" + safeName;

        try {
            Path targetPath = this.storageDirectory.resolve(fileId).normalize();
            if (!targetPath.startsWith(this.storageDirectory.normalize())) {
                throw new SecurityException("Path traversal attempt detected");
            }

            InputStream is = file.getInputStream();
            if (is == null && file.getBytes() != null) {
                is = new ByteArrayInputStream(file.getBytes());
            }

            if (is != null) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                throw new BadRequestException("No file content provided");
            }

            log.info("Stored resume file metadata: size={} bytes for user={}", file.getSize(), userId);
            
            return StoredFile.builder()
                    .fileId(fileId)
                    .fileName(safeName)
                    .fileUrl(targetPath.toUri().toString())
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .size(file.getSize())
                    .createdAt(Instant.now())
                    .build();
        } catch (IOException | SecurityException e) {
            log.error("Failed to store resume file: {}", e.getMessage());
            throw new BadRequestException("Failed to store uploaded file securely");
        }
    }

    @Override
    public StoredFile get(String fileId) {
        try {
            Path path = resolvePath(fileId);
            if (!Files.exists(path)) {
                return null;
            }
            byte[] bytes = Files.readAllBytes(path);
            return StoredFile.builder()
                    .fileId(fileId)
                    .fileName(path.getFileName().toString())
                    .fileUrl(path.toUri().toString())
                    .size(bytes.length)
                    .content(bytes)
                    .build();
        } catch (Exception e) {
            log.error("Failed to load file from storage: {}", e.getMessage());
            throw new BadRequestException("Could not retrieve stored file");
        }
    }

    @Override
    public void delete(String fileId) {
        try {
            Path path = resolvePath(fileId);
            Files.deleteIfExists(path);
            log.info("Deleted stored file: {}", fileId);
        } catch (Exception e) {
            log.warn("Could not delete file at {}: {}", fileId, e.getMessage());
        }
    }

    private Path resolvePath(String fileIdentifier) {
        if (fileIdentifier.startsWith("file:/")) {
            return Paths.get(java.net.URI.create(fileIdentifier));
        }
        return this.storageDirectory.resolve(fileIdentifier).normalize();
    }
}
