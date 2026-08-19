package com.jobpilot.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminDashboardStatsDto {
    private long totalUsers;
    private long totalApplications;
    
    private JobSourceHealth jobSourceHealth;
    private AiUsageMetrics aiUsage;
    private WorkerHealth workerHealth;
    private SchedulerStatus schedulerStatus;
    
    @Data
    @Builder
    public static class JobSourceHealth {
        private long totalSearchRuns;
        private long totalFailures;
        private Map<String, Long> failuresBySource;
    }
    
    @Data
    @Builder
    public static class AiUsageMetrics {
        private long totalRequests;
        private Map<String, Long> requestsByProvider;
        private double averageLatencyMs;
    }
    
    @Data
    @Builder
    public static class WorkerHealth {
        private String status;
        private int activeWorkers;
        private long totalFailedApplications;
    }
    
    @Data
    @Builder
    public static class SchedulerStatus {
        private String status;
        private String lastRunTime;
        private String nextScheduledRun;
    }
}
