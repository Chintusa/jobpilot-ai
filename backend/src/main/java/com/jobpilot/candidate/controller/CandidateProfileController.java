package com.jobpilot.candidate.controller;

import com.jobpilot.candidate.dto.CandidateProfileDto;
import com.jobpilot.candidate.dto.UpdateProfileRequest;
import com.jobpilot.candidate.service.CandidateProfileService;
import com.jobpilot.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/candidate-profile", "/api/v1/candidate-profile", "/api/v1/profile"})
@RequiredArgsConstructor
@Tag(name = "Candidate Profile", description = "Endpoints for managing candidate profile, experience, skills, and education")
public class CandidateProfileController {

    private final CandidateProfileService profileService;

    @GetMapping
    @Operation(summary = "Get current candidate's structured profile")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        CandidateProfileDto dto = profileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping
    @Operation(summary = "Update and approve candidate profile information")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        CandidateProfileDto dto = profileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile saved and updated successfully", dto));
    }

    @PostMapping("/skills")
    @Operation(summary = "Add a new skill to candidate profile")
    public ResponseEntity<ApiResponse<CandidateProfileDto>> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String skillName,
            @RequestParam(value = "category", required = false) String category) {
        CandidateProfileDto dto = profileService.addSkill(userDetails.getUsername(), skillName, category);
        return ResponseEntity.ok(ApiResponse.ok("Skill added", dto));
    }
}
