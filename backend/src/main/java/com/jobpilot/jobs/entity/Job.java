package com.jobpilot.jobs.entity;

import com.jobpilot.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private JobSource source;

    @Column(name = "external_id")
    private String externalId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    @Column(name = "work_mode", nullable = false)
    @Builder.Default
    private String workMode = "HYBRID";

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency")
    @Builder.Default
    private String salaryCurrency = "INR";

    @Column(name = "salary_display")
    private String salaryDisplay;

    @Column(name = "experience_min", precision = 4, scale = 1)
    private BigDecimal experienceMin;

    @Column(name = "experience_max", precision = 4, scale = 1)
    private BigDecimal experienceMax;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    @Builder.Default
    private String requiredSkills = "[]";

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    @Builder.Default
    private String preferredSkills = "[]";

    @Column(name = "job_url", length = 1024)
    private String jobUrl;

    @Column(name = "canonical_url", length = 1024)
    private String canonicalUrl;

    @Column(name = "dedup_hash", length = 255)
    private String dedupHash;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "posted_at")
    @Builder.Default
    private Instant postedAt = Instant.now();
}
