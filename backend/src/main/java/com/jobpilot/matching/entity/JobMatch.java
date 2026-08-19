package com.jobpilot.matching.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_matches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore;

    @Column(nullable = false)
    @Builder.Default
    private String classification = "GOOD"; // EXCELLENT, STRONG, GOOD, POSSIBLE, LOW

    @Column(nullable = false)
    @Builder.Default
    private String recommendation = "REVIEW"; // APPLY, REVIEW, SKIP

    @Column(name = "score_breakdown", columnDefinition = "TEXT")
    @Builder.Default
    private String scoreBreakdown = "{}";

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(nullable = false)
    @Builder.Default
    private String status = "UNVIEWED"; // UNVIEWED, VIEWED, SAVED, DISMISSED
}
