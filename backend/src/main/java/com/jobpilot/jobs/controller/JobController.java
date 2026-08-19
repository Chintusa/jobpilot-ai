package com.jobpilot.jobs.controller;

import com.jobpilot.common.dto.ApiResponse;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.service.JobDiscoveryService;
import com.jobpilot.jobs.service.JobService;
import com.jobpilot.jobs.source.JobSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/jobs", "/api/v1/jobs"})
@RequiredArgsConstructor
@Tag(name = "Job Discovery", description = "Endpoints for discovering, searching, syncing, and inspecting verified jobs")
public class JobController {

    private final JobService jobService;
    private final JobDiscoveryService jobDiscoveryService;

    @GetMapping
    @Operation(summary = "Search and filter job opportunities")
    public ResponseEntity<ApiResponse<Page<JobDto>>> searchJobs(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "workMode", required = false) String workMode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<JobDto> result = jobService.searchJobs(keyword, location, workMode,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt")));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/sync")
    @Operation(summary = "Run modular Job Discovery pipeline (MockJobSource -> JobNormalizer -> PostgreSQL -> Matching Engine)")
    public ResponseEntity<ApiResponse<List<JobDto>>> syncJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) JobSearchCriteria criteria) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        JobSearchCriteria searchCriteria = criteria != null ? criteria : JobSearchCriteria.builder().limit(20).build();
        List<JobDto> discovered = jobDiscoveryService.runDiscoveryPipeline(email, searchCriteria);
        return ResponseEntity.ok(ApiResponse.ok("Discovered and normalized " + discovered.size() + " opportunities", discovered));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed job specification by UUID")
    public ResponseEntity<ApiResponse<JobDto>> getJobById(@PathVariable("id") UUID id) {
        JobDto dto = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
