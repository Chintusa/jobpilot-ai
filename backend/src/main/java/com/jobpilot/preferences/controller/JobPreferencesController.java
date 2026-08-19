package com.jobpilot.preferences.controller;

import com.jobpilot.common.dto.ApiResponse;
import com.jobpilot.preferences.dto.JobPreferencesDto;
import com.jobpilot.preferences.dto.UpdateJobPreferencesRequest;
import com.jobpilot.preferences.service.JobPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/preferences", "/api/v1/preferences", "/api/v1/job-preferences"})
@RequiredArgsConstructor
@Tag(name = "Job Preferences", description = "Endpoints for configuring target roles, locations, exclusions, matching thresholds, and auto-apply policies")
public class JobPreferencesController {

    private final JobPreferencesService preferencesService;

    @GetMapping
    @Operation(summary = "Get current candidate search and auto-apply preferences")
    public ResponseEntity<ApiResponse<JobPreferencesDto>> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        JobPreferencesDto dto = preferencesService.getPreferences(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping
    @Operation(summary = "Update search criteria, exclusions, salary limits, and auto-apply guardrails")
    public ResponseEntity<ApiResponse<JobPreferencesDto>> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateJobPreferencesRequest request) {
        JobPreferencesDto dto = preferencesService.updatePreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Preferences saved successfully", dto));
    }
}
