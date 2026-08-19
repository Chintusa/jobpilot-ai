package com.jobpilot.automation.controller;

import com.jobpilot.automation.dto.HumanInterventionDto;
import com.jobpilot.automation.dto.ResolveInterventionRequest;
import com.jobpilot.automation.service.AutomationService;
import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/interventions", "/api/v1/interventions"})
@RequiredArgsConstructor
@Tag(name = "Human Intervention Center", description = "Endpoints for handling human-in-the-loop pause triggers, context inspection, and candidate resolution")
public class InterventionController {

    private final AutomationService automationService;

    @GetMapping
    @Operation(summary = "Get list of pending or resolved interventions")
    public ResponseEntity<ApiResponse<List<HumanInterventionDto>>> getInterventions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "status", required = false) String status) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        List<HumanInterventionDto> list = automationService.getUserInterventions(email, status);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Inspect intervention context, reason, and required input")
    public ResponseEntity<ApiResponse<HumanInterventionDto>> getInterventionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        HumanInterventionDto dto = automationService.getInterventionById(email, id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Provide candidate resolution and resume application submission")
    public ResponseEntity<ApiResponse<HumanInterventionDto>> resolveIntervention(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) ResolveInterventionRequest request) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        HumanInterventionDto dto = automationService.resolveIntervention(email, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Intervention resolved; application resumed", dto));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel intervention flow and return application to matched state")
    public ResponseEntity<ApiResponse<HumanInterventionDto>> cancelIntervention(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id) {
        String email = userDetails != null ? userDetails.getUsername() : "jhasaketan@example.com";
        HumanInterventionDto dto = automationService.cancelIntervention(email, id);
        return ResponseEntity.ok(ApiResponse.ok("Intervention cancelled", dto));
    }
}
