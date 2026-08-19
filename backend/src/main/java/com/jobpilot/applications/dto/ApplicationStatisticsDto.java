package com.jobpilot.applications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsDto {
    private long totalApplications;
    private long preparingCount;
    private long readyForReviewCount;
    private long submittedCount;
    private long interviewCount;
    private long offerCount;
    private long rejectedCount;
    private long withdrawnCount;
    private long failedCount;
    private double interviewRate;
    private double offerRate;
    private double successRate;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> sourceBreakdown;
}
