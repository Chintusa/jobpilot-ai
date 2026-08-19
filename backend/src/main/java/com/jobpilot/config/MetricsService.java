package com.jobpilot.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementJobSearchRun(String source) {
        Counter.builder("jobpilot.jobsearch.runs")
                .description("Number of job search runs executed")
                .tag("source", source)
                .register(meterRegistry)
                .increment();
    }

    public void incrementSourceFailure(String source, String errorType) {
        Counter.builder("jobpilot.source.failures")
                .description("Number of failures from external job sources")
                .tag("source", source)
                .tag("errorType", errorType)
                .register(meterRegistry)
                .increment();
    }

    public void incrementAiRequest(String provider, String operation) {
        Counter.builder("jobpilot.ai.requests")
                .description("Number of AI requests made")
                .tag("provider", provider)
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public <T> T recordAiLatency(String provider, String operation, Callable<T> callable) throws Exception {
        Timer timer = Timer.builder("jobpilot.ai.latency")
                .description("Latency of AI operations")
                .tag("provider", provider)
                .tag("operation", operation)
                .register(meterRegistry);

        return timer.recordCallable(callable);
    }

    public void incrementApplicationExecution(String status) {
        Counter.builder("jobpilot.application.executions")
                .description("Number of application executions")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void incrementWorkerFailure(String reason) {
        Counter.builder("jobpilot.worker.failures")
                .description("Number of application worker failures")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void incrementSchedulerRun(String taskName) {
        Counter.builder("jobpilot.scheduler.runs")
                .description("Number of scheduled task runs")
                .tag("taskName", taskName)
                .register(meterRegistry)
                .increment();
    }
}
