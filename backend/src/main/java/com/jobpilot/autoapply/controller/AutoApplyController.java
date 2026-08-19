package com.jobpilot.autoapply.controller;

import com.jobpilot.autoapply.dto.AutoApplyDecisionDto;
import com.jobpilot.autoapply.dto.AutoApplyEvaluationResultDto;
import com.jobpilot.autoapply.dto.AutoApplyPolicyDto;
import com.jobpilot.autoapply.dto.UpdateAutoApplyPolicyRequest;
import com.jobpilot.autoapply.service.AutoApplyService;
import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/auto-apply", "/api/v1/auto-apply"})
@RequiredArgsConstructor
@Tag(name = "Controlled Auto-Apply", description = "Endpoints for deterministic auto-apply policy configuration, evaluation, execution, and audit trail")
public class AutoApplyController {

    private final AutoApplyService autoApplyService;

    @GetMapping("/policy")
    @Operation(summary = "Get candidate auto-apply policy settings")
    public ResponseEntity<ApiResponse<AutoApplyPolicyDto>> getPolicy(
            @AuthenticationPrincipal UserDetails userDetails) {
        AutoApplyPolicyDto policy = autoApplyService.getPolicy(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(policy));
    }

    @PutMapping("/policy")
    @Operation(summary = "Update candidate auto-apply policy parameters")
    public ResponseEntity<ApiResponse<AutoApplyPolicyDto>> updatePolicy(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateAutoApplyPolicyRequest request) {
        AutoApplyPolicyDto policy = autoApplyService.updatePolicy(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Auto-apply policy updated successfully", policy));
    }

    @PostMapping("/evaluate/{jobId}")
    @Operation(summary = "Deterministically evaluate auto-apply eligibility and record decision audit log")
    public ResponseEntity<ApiResponse<AutoApplyEvaluationResultDto>> evaluateJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId) {
        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userDetails.getUsername(), jobId);
        return ResponseEntity.ok(ApiResponse.ok("Deterministic auto-apply evaluation completed", result));
    }

    @PostMapping("/process/{jobId}")
    @Operation(summary = "Evaluate and execute controlled auto-apply workflow under safety and approval constraints")
    public ResponseEntity<ApiResponse<AutoApplyEvaluationResultDto>> processAutoApply(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId) {
        AutoApplyEvaluationResultDto result = autoApplyService.processAutoApply(userDetails.getUsername(), jobId);
        return ResponseEntity.ok(ApiResponse.ok("Auto-apply workflow processed", result));
    }

    @GetMapping("/decisions")
    @Operation(summary = "Retrieve complete audit history of auto-apply decisions")
    public ResponseEntity<ApiResponse<List<AutoApplyDecisionDto>>> getDecisions(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AutoApplyDecisionDto> decisions = autoApplyService.getUserDecisions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(decisions));
    }

    @GetMapping("/decisions/{jobId}")
    @Operation(summary = "Retrieve audit history of auto-apply decisions for a specific job")
    public ResponseEntity<ApiResponse<List<AutoApplyDecisionDto>>> getDecisionsForJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID jobId) {
        List<AutoApplyDecisionDto> decisions = autoApplyService.getDecisionsForJob(userDetails.getUsername(), jobId);
        return ResponseEntity.ok(ApiResponse.ok(decisions));
    }
}
