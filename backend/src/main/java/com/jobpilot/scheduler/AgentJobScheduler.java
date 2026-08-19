package com.jobpilot.scheduler;

import com.jobpilot.automation.entity.AgentSchedule;
import com.jobpilot.automation.repository.AgentScheduleRepository;
import com.jobpilot.jobs.service.JobDiscoveryService;
import com.jobpilot.jobs.source.JobSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentJobScheduler {

    private final AgentScheduleRepository scheduleRepository;
    private final JobDiscoveryService jobDiscoveryService;

    /**
     * Periodic background discovery run (every 30 minutes).
     */
    @Scheduled(cron = "${app.scheduler.job-discovery-cron:0 0/30 * * * ?}")
    public void runScheduledJobDiscovery() {
        List<AgentSchedule> activeSchedules = scheduleRepository.findAll().stream()
                .filter(s -> s.isEnabled() && "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .toList();

        if (activeSchedules.isEmpty()) {
            log.debug("No active agent schedules to execute at {}", Instant.now());
            return;
        }

        log.info("Executing scheduled autonomous search for {} active candidates", activeSchedules.size());

        for (AgentSchedule schedule : activeSchedules) {
            try {
                String userEmail = schedule.getUser().getEmail();
                JobSearchCriteria criteria = JobSearchCriteria.builder()
                        .limit(10)
                        .build();

                jobDiscoveryService.runDiscoveryPipeline(userEmail, criteria);

                schedule.setLastRunAt(Instant.now());
                schedule.setNextRunAt(Instant.now().plusSeconds(1800));
                scheduleRepository.save(schedule);
            } catch (Exception e) {
                log.error("Failed scheduled search for user {}: {}", schedule.getUser().getEmail(), e.getMessage());
            }
        }
    }
}
