package com.jobpilot.preferences.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobPreferencesRequest {

    private List<String> targetRoles;
    private List<String> roleVariations;
    private List<String> locations;
    private List<String> workModes;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal minExperience;
    private BigDecimal maxExperience;
    private List<String> industries;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private List<String> excludedCompanies;
    private List<String> excludedKeywords;
    private List<String> jobTypes;

    private Boolean autoApplyEnabled;

    @Min(value = 50, message = "Matching threshold must be at least 50%")
    @Max(value = 100, message = "Matching threshold cannot exceed 100%")
    private Integer autoApplyMinScore;

    @Min(value = 1, message = "Daily limit must be at least 1")
    @Max(value = 100, message = "Daily limit cannot exceed 100")
    private Integer autoApplyDailyLimit;

    private Boolean requireApproval;
}
