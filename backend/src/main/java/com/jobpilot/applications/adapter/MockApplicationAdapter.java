package com.jobpilot.applications.adapter;

import com.jobpilot.jobs.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MockApplicationAdapter implements ApplicationAdapter {

    @Override
    public boolean supports(Job job) {
        return job != null;
    }

    @Override
    public ApplicationResult execute(ApplicationContext context) {
        log.info("Executing ApplicationAdapter for application: {}", context.getApplicationId());

        return ApplicationResult.builder()
                .applicationId(context.getApplicationId())
                .status("SUBMITTED")
                .submissionResult("CONFIRMED_ONLINE_PORTAL")
                .screenshots(List.of())
                .logs(List.of("Application successfully dispatched via ApplicationAdapter"))
                .executionTimeMs(250L)
                .build();
    }
}
