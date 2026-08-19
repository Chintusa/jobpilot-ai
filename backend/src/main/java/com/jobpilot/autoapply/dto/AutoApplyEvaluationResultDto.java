package com.jobpilot.autoapply.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoApplyEvaluationResultDto {
    private UUID jobId;
    private String jobTitle;
    private String company;
    private Integer score;
    private boolean eligible;
    private String decision;
    private String decisionReason;
    private Map<String, Object> policyValues;
    private List<String> passedConditions;
    private List<String> failedConditions;
    private UUID applicationId;
    private String applicationStatus;
    private String applicationPreparationState;
    private Instant timestamp;
}
