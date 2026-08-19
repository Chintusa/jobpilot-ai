package com.jobpilot.applications.controller;

import com.jobpilot.applications.dto.*;
import com.jobpilot.applications.service.ApplicationPreparationAgentService;
import com.jobpilot.applications.service.ApplicationService;
import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/applications", "/api/v1/applications"})
@RequiredArgsConstructor
@Tag(name = "Application Preparation Agent & Tracking", description = "Endpoints for AI agent preparation, content review, editing, submission, lifecycle timeline tracking, and worker callbacks")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationPreparationAgentService preparationAgentService;

    @GetMapping
    @Operation(summary = "Get list of candidate applications with multi-criteria filtering and sorting")
    public ResponseEntity<ApiResponse<List<ApplicationDto>>> getApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "startDate", required = false) Instant startDate,
            @RequestParam(value = "endDate", required = false) Instant endDate,
            @RequestParam(value = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "DESC") String sortDirection) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";

        ApplicationFilterRequest filter = ApplicationFilterRequest.builder()
                .status(status)
                .source(source)
                .company(company)
                .query(query)
                .startDate(startDate)
                .endDate(endDate)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        List<ApplicationDto> apps = applicationService.getFilteredApplications(email, filter);
        return ResponseEntity.ok(ApiResponse.ok(apps));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complete application details, tailored content, and screening answers")
    public ResponseEntity<ApiResponse<ApplicationDto>> getApplicationDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = applicationService.getApplicationDetail(email, applicationId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping({"/{id}/timeline", "/{id}/events"})
    @Operation(summary = "Get chronological lifecycle event timeline for an application")
    public ResponseEntity<ApiResponse<List<ApplicationEventDto>>> getApplicationTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        List<ApplicationEventDto> timeline = applicationService.getApplicationTimeline(email, applicationId);
        return ResponseEntity.ok(ApiResponse.ok(timeline));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update application status (e.g. INTERVIEW, OFFER, REJECTED, WITHDRAWN) and record audit milestone")
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplicationStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = applicationService.updateApplicationStatus(email, applicationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Application status updated successfully", dto));
    }

    @PostMapping("/{id}/worker-callback")
    @Operation(summary = "Receive browser automation worker outcome and audit trail")
    public ResponseEntity<ApiResponse<ApplicationDto>> recordWorkerCallback(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId,
            @RequestBody WorkerCallbackRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = applicationService.recordWorkerResult(email, applicationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Worker result recorded in lifecycle timeline", dto));
    }

    @GetMapping({"/statistics", "/stats"})
    @Operation(summary = "Get candidate application funnel statistics and conversion rates")
    public ResponseEntity<ApiResponse<ApplicationStatisticsDto>> getApplicationStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationStatisticsDto stats = applicationService.getApplicationStatistics(email);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @PostMapping({"/prepare/{jobId}", "/prep/{jobId}"})
    @Operation(summary = "Execute AI Application Preparation Agent for a job opportunity")
    public ResponseEntity<ApiResponse<ApplicationDto>> prepareApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("jobId") UUID jobId) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = preparationAgentService.prepareApplication(email, jobId);
        return ResponseEntity.ok(ApiResponse.ok("Application prepared with tailored assets", dto));
    }

    @PutMapping("/{id}/content")
    @Operation(summary = "Review and edit generated application content (cover letter, tailored resume, screening answers)")
    public ResponseEntity<ApiResponse<ApplicationDto>> updateApplicationContent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId,
            @RequestBody UpdateApplicationContentRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = preparationAgentService.updateApplicationContent(email, applicationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Application content updated successfully", dto));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Confirm user review and submit application")
    public ResponseEntity<ApiResponse<ApplicationDto>> submitApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID applicationId) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        ApplicationDto dto = applicationService.submitApplication(email, applicationId);
        return ResponseEntity.ok(ApiResponse.ok("Application submitted successfully", dto));
    }
}
