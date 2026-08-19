package com.jobpilot.autoapply.entity;

import com.jobpilot.applications.entity.Application;
import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "auto_apply_decisions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoApplyDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "policy_values", nullable = false, columnDefinition = "TEXT")
    private String policyValues;

    @Column(name = "passed_conditions", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String passedConditions = "[]";

    @Column(name = "failed_conditions", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String failedConditions = "[]";

    @Column(nullable = false)
    private String decision;

    @Column(name = "decision_reason", nullable = false, columnDefinition = "TEXT")
    private String decisionReason;

    @Column(nullable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();
}
