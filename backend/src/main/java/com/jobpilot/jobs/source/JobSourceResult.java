package com.jobpilot.jobs.source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSourceResult {

    private String sourceName;
    private boolean success;
    private String errorMessage;
    private int jobsDiscovered;
    private int jobsPersisted;
    private int duplicatesSkipped;
    private long syncDurationMs;

    @Builder.Default
    private List<ExternalJob> jobs = new ArrayList<>();

    public static JobSourceResult success(String sourceName, List<ExternalJob> jobs, long durationMs) {
        return JobSourceResult.builder()
                .sourceName(sourceName)
                .success(true)
                .jobsDiscovered(jobs != null ? jobs.size() : 0)
                .syncDurationMs(durationMs)
                .jobs(jobs != null ? jobs : List.of())
                .build();
    }

    public static JobSourceResult failure(String sourceName, String errorMessage, long durationMs) {
        return JobSourceResult.builder()
                .sourceName(sourceName)
                .success(false)
                .errorMessage(errorMessage)
                .jobsDiscovered(0)
                .syncDurationMs(durationMs)
                .jobs(List.of())
                .build();
    }
}
