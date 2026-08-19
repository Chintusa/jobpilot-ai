package com.jobpilot.applications.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResult {
    private UUID applicationId;
    private String status; // SUBMITTED, HUMAN_INTERVENTION_REQUIRED, FAILED
    private String submissionResult;
    private String interventionReason;
    private String interventionDescription;
    private String failureReason;
    @Builder.Default
    private List<String> screenshots = new ArrayList<>();
    @Builder.Default
    private List<String> logs = new ArrayList<>();
    private Long executionTimeMs;
}
