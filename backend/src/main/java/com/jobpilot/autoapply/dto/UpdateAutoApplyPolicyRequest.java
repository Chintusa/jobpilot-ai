package com.jobpilot.autoapply.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAutoApplyPolicyRequest {
    private Boolean enabled;

    @Min(value = 0, message = "Minimum score must be at least 0")
    @Max(value = 100, message = "Minimum score cannot exceed 100")
    private Integer minimumScore;

    private Boolean requireApproval;

    @Min(value = 1, message = "Daily limit must be at least 1")
    @Max(value = 100, message = "Daily limit cannot exceed 100")
    private Integer maxApplicationsPerDay;
}
