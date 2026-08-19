package com.jobpilot.resume.controller;

import com.jobpilot.common.dto.ApiResponse;
import com.jobpilot.resume.dto.ResumeDto;
import com.jobpilot.resume.dto.ResumeProcessResponse;
import com.jobpilot.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/resumes", "/api/v1/resumes"})
@RequiredArgsConstructor
@Tag(name = "Resume Intelligence", description = "Endpoints for uploading, parsing, AI extraction, and activating resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload candidate resume document")
    public ResponseEntity<ApiResponse<ResumeDto>> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        ResumeDto dto = resumeService.uploadResume(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resume uploaded successfully", dto));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload candidate resume document (alias)")
    public ResponseEntity<ApiResponse<ResumeDto>> uploadResumeAlias(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        return uploadResume(userDetails, file);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Trigger AI Extraction pipeline on uploaded resume to build candidate profile")
    public ResponseEntity<ApiResponse<ResumeProcessResponse>> processResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID resumeId) {
        ResumeProcessResponse response = resumeService.processResume(userDetails.getUsername(), resumeId);
        return ResponseEntity.ok(ApiResponse.ok("Resume processed and candidate profile extracted successfully", response));
    }

    @GetMapping
    @Operation(summary = "List all resumes belonging to candidate")
    public ResponseEntity<ApiResponse<List<ResumeDto>>> getResumes(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeDto> resumes = resumeService.getUserResumes(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(resumes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific resume details")
    public ResponseEntity<ApiResponse<ResumeDto>> getResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID resumeId) {
        ResumeDto resume = resumeService.getResumeById(userDetails.getUsername(), resumeId);
        return ResponseEntity.ok(ApiResponse.ok(resume));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Set specific resume as primary active application document")
    public ResponseEntity<ApiResponse<ResumeDto>> activateResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID resumeId) {
        ResumeDto dto = resumeService.setActiveResume(userDetails.getUsername(), resumeId);
        return ResponseEntity.ok(ApiResponse.ok("Resume activated", dto));
    }
}
