package com.jobpilot.automation.controller;

import com.jobpilot.automation.dto.AgentStatusDto;
import com.jobpilot.automation.service.AutomationService;
import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent Control Center", description = "Endpoints for checking agent status and toggling autonomous cycles")
public class AgentControlCenterController {

    private final AutomationService automationService;

    @GetMapping("/status")
    @Operation(summary = "Get current autonomous AI Agent status")
    public ResponseEntity<ApiResponse<AgentStatusDto>> getAgentStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        AgentStatusDto dto = automationService.getAgentStatus(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/toggle")
    @Operation(summary = "Activate or pause autonomous AI Agent cycles")
    public ResponseEntity<ApiResponse<AgentStatusDto>> toggleAgent(
            @AuthenticationPrincipal UserDetails userDetails) {
        AgentStatusDto dto = automationService.toggleAgent(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Agent status updated", dto));
    }
}
