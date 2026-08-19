package com.jobpilot.jobs.source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalJob {

    private String externalId;
    private String sourceName;
    private String rawTitle;
    private String rawCompany;
    private String rawLocation;
    private String rawWorkMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String salaryDisplay;
    private BigDecimal experienceMin;
    private BigDecimal experienceMax;
    private String rawDescription;
    private List<String> rawSkills;
    private String jobUrl;
    @Builder.Default
    private Instant postedAt = Instant.now();
}
