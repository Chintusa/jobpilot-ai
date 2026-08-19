package com.jobpilot.resume.dto;

import com.jobpilot.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDto {

    private UUID id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private Integer atsScore;
    private String status;
    private boolean active;
    private Instant createdAt;

    public static ResumeDto fromEntity(Resume resume) {
        if (resume == null) return null;
        return ResumeDto.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .fileUrl(resume.getFileUrl())
                .fileSize(resume.getFileSize())
                .contentType(resume.getContentType())
                .atsScore(resume.getAtsScore())
                .status(resume.getStatus())
                .active(resume.isActive())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
