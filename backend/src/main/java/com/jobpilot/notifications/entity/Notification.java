package com.jobpilot.notifications.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private String type = "INFO"; // INFO, WARNING, SUCCESS, INTERVENTION

    @Column(nullable = false)
    @Builder.Default
    private String category = "GENERAL"; // HIGH_MATCH_JOB, HUMAN_INTERVENTION, APPLICATION_SUBMITTED, APPLICATION_FAILED, INTERVIEW_SCHEDULED, DEADLINE_APPROACHING, STATUS_CHANGE, GENERAL

    @Column(name = "dedup_key")
    private String dedupKey;

    @Column(nullable = false)
    @Builder.Default
    private String channel = "IN_APP"; // IN_APP, EMAIL, BOTH

    @Column(name = "email_delivered")
    @Builder.Default
    private boolean emailDelivered = false;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String metadata = "{}";

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type")
    private String referenceType; // JOB, APPLICATION, INTERVENTION
}
