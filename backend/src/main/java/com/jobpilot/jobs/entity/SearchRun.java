package com.jobpilot.jobs.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "search_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private String status = "COMPLETED"; // IN_PROGRESS, COMPLETED, FAILED

    @Column(name = "search_strategies", columnDefinition = "TEXT")
    @Builder.Default
    private String searchStrategies = "[]";

    @Column(name = "role_variations", columnDefinition = "TEXT")
    @Builder.Default
    private String roleVariations = "[]";

    @Column(name = "sources_queried", columnDefinition = "TEXT")
    @Builder.Default
    private String sourcesQueried = "[]";

    private String query;

    @Column(name = "number_found", nullable = false)
    @Builder.Default
    private int numberFound = 0;

    @Column(name = "duplicates_removed", nullable = false)
    @Builder.Default
    private int duplicatesRemoved = 0;

    @Column(name = "filtered_jobs", nullable = false)
    @Builder.Default
    private int filteredJobs = 0;

    @Column(name = "matched_jobs", nullable = false)
    @Builder.Default
    private int matchedJobs = 0;

    @Column(name = "recommended_jobs", nullable = false)
    @Builder.Default
    private int recommendedJobs = 0;

    @Column(columnDefinition = "TEXT")
    private String errors;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_ms", nullable = false)
    @Builder.Default
    private long durationMs = 0L;

    @Column(name = "audit_log", columnDefinition = "TEXT")
    private String auditLog;
}
