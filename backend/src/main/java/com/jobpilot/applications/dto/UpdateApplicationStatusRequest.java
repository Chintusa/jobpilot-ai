package com.jobpilot.applications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {
    @NotBlank(message = "Status cannot be blank")
    private String status; // DRAFT, PREPARING, READY_FOR_REVIEW, APPROVED, SUBMITTING, SUBMITTED, FAILED, REQUIRES_USER_INPUT, WITHDRAWN, REJECTED, INTERVIEW, OFFER

    private String note;
    private String source; // CANDIDATE_USER, RECRUITER, EMPLOYER_PORTAL
    private Map<String, Object> metadata;
}
