package com.jobpilot.admin.service;

import com.jobpilot.admin.dto.AdminDashboardStatsDto;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.user.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final MeterRegistry meterRegistry;

    public AdminDashboardStatsDto getDashboardStats() {
        log.debug("Fetching admin dashboard statistics");
        
        long totalUsers = userRepository.count();
        long totalApplications = applicationRepository.count();

        return AdminDashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalApplications(totalApplications)
                .jobSourceHealth(getJobSourceHealth())
                .aiUsage(getAiUsageMetrics())
                .workerHealth(getWorkerHealth())
                .schedulerStatus(getSchedulerStatus())
                .build();
    }

    private AdminDashboardStatsDto.JobSourceHealth getJobSourceHealth() {
        // Fetch from Micrometer if available, otherwise default to 0
        long searchRuns = 0;
        try {
            searchRuns = (long) meterRegistry.get("jobpilot.jobsearch.runs").counter().count();
        } catch (Exception e) {
            log.trace("Counter jobpilot.jobsearch.runs not found or empty");
        }
        
        long failures = 0;
        try {
            failures = (long) meterRegistry.get("jobpilot.source.failures").counter().count();
        } catch (Exception e) {
             log.trace("Counter jobpilot.source.failures not found or empty");
        }

        Map<String, Long> failuresBySource = new HashMap<>();
        // In a real scenario, we could iterate over tags, for now we keep it simple
        
        return AdminDashboardStatsDto.JobSourceHealth.builder()
                .totalSearchRuns(searchRuns)
                .totalFailures(failures)
                .failuresBySource(failuresBySource)
                .build();
    }

    private AdminDashboardStatsDto.AiUsageMetrics getAiUsageMetrics() {
        long requests = 0;
        try {
            requests = (long) meterRegistry.get("jobpilot.ai.requests").counter().count();
        } catch (Exception e) {
            // ignore
        }
        
        double avgLatency = 0.0;
        try {
            avgLatency = meterRegistry.get("jobpilot.ai.latency").timer().mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // ignore
        }

        return AdminDashboardStatsDto.AiUsageMetrics.builder()
                .totalRequests(requests)
                .averageLatencyMs(Double.isNaN(avgLatency) ? 0.0 : avgLatency)
                .requestsByProvider(new HashMap<>())
                .build();
    }

    private AdminDashboardStatsDto.WorkerHealth getWorkerHealth() {
        return AdminDashboardStatsDto.WorkerHealth.builder()
                .status("HEALTHY")
                .activeWorkers(1) // Mock active worker count
                .totalFailedApplications(0) // Mock total failures
                .build();
    }

    private AdminDashboardStatsDto.SchedulerStatus getSchedulerStatus() {
        return AdminDashboardStatsDto.SchedulerStatus.builder()
                .status("ACTIVE")
                .lastRunTime("2026-08-17T12:00:00Z") // Placeholder
                .nextScheduledRun("2026-08-17T13:00:00Z") // Placeholder
                .build();
    }
}
