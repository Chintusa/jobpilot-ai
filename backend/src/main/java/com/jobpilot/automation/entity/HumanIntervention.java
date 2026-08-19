package com.jobpilot.automation.entity;

import com.jobpilot.applications.entity.Application;
import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "human_interventions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanIntervention extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String reason; // CAPTCHA, MFA, UNKNOWN_QUESTION, LEGAL_DECLARATION, MISSING_INFO, UNSUPPORTED_FLOW, ACCESS_CONTROL

    private String type; // Alias for reason

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, IN_PROGRESS, RESOLVED, CANCELLED

    @Column(name = "required_input")
    @Builder.Default
    private String requiredInput = "TEXT";

    @Column(name = "required_input_type")
    @Builder.Default
    private String requiredInputType = "TEXT";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String context = "{}";

    @Column(name = "resolution_payload", columnDefinition = "TEXT")
    private String resolutionPayload;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
