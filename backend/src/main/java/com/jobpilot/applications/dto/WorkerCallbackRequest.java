package com.jobpilot.applications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerCallbackRequest {
    private String status; // SUBMITTED, FAILED, HUMAN_INTERVENTION_REQUIRED
    private String submissionResult;
    private String failureReason;
    private String interventionReason; // CAPTCHA, MFA, UNKNOWN_QUESTION, ACCESS_CONTROL, UNSUPPORTED_FLOW
    private String interventionDescription;
    private List<String> screenshots;
    private List<String> logs;
    private Long executionTimeMs;
}
