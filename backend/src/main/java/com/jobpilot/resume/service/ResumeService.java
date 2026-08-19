package com.jobpilot.resume.service;

import com.jobpilot.common.exception.BadRequestException;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.resume.dto.ResumeDto;
import com.jobpilot.resume.dto.ResumeProcessResponse;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
import com.jobpilot.resume.storage.FileStorageService;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeParserService resumeParserService;
    private final FileStorageService fileStorageService;
    private final ResumeAiExtractionService resumeAiExtractionService;

    @Transactional
    public ResumeDto uploadResume(String userEmail, MultipartFile file) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BadRequestException("File name is missing");
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("pdf") && !extension.equals("docx")) {
            throw new BadRequestException("Invalid file extension. Only .pdf and .docx files are permitted");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && 
            !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new BadRequestException("Invalid content type. Only PDF and DOCX are allowed");
        }

        // 1. Secure file validation & parsing with Apache PDFBox (PDF) / Apache POI (DOCX)
        // Resume contents are not logged
        String parsedText = resumeParserService.parseResume(file);

        // 2. Persist to storage abstraction
        String storageUrl = fileStorageService.storeFile(file, user.getId());

        // 3. Set active if first resume
        List<Resume> existingResumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        boolean makeActive = existingResumes.isEmpty();

        Resume resume = Resume.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .fileUrl(storageUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .parsedText(parsedText)
                .atsScore(92)
                .status("PARSED")
                .active(makeActive)
                .build();

        Resume saved = resumeRepository.save(resume);
        log.info("Saved resume record: id={}, status={}, active={}", saved.getId(), saved.getStatus(), saved.isActive());

        return ResumeDto.fromEntity(saved);
    }

    @Transactional
    public ResumeProcessResponse processResume(String userEmail, UUID resumeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Resume does not belong to the current authenticated user");
        }

        resume.setStatus("PROCESSING");
        resumeRepository.save(resume);

        return resumeAiExtractionService.extractAndPopulateCandidateProfile(user, resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> getUserResumes(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ResumeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumeDto getResumeById(String userEmail, UUID resumeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));
        
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Resume does not belong to the current authenticated user");
        }
        
        return ResumeDto.fromEntity(resume);
    }

    @Transactional
    public ResumeDto setActiveResume(String userEmail, UUID resumeId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Resume targetResume = null;

        for (Resume r : resumes) {
            if (r.getId().equals(resumeId)) {
                r.setActive(true);
                targetResume = r;
            } else {
                r.setActive(false);
            }
        }

        if (targetResume == null) {
            throw new ResourceNotFoundException("Resume not found with id: " + resumeId);
        }

        resumeRepository.saveAll(resumes);
        return ResumeDto.fromEntity(targetResume);
    }
}
