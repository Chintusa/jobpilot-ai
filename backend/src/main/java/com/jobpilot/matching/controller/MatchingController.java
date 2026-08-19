package com.jobpilot.matching.controller;

import com.jobpilot.common.dto.ApiResponse;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.service.MatchingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/jobs/{jobId}/match", "/api/v1/jobs/{jobId}/match"})
@RequiredArgsConstructor
@Tag(name = "AI Matching Engine", description = "Endpoints for computing and inspecting granular recruiter match scores")
public class MatchingController {

    private final MatchingEngineService matchingService;

    @GetMapping
    @Operation(summary = "Calculate and retrieve AI recruiter match assessment for job")
    public ResponseEntity<ApiResponse<JobMatchDto>> getJobMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("jobId") UUID jobId) {
        JobMatchDto dto = matchingService.calculateOrGetMatch(userDetails.getUsername(), jobId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
