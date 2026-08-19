package com.jobpilot.notifications.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_notifications_enabled", nullable = false)
    @Builder.Default
    private boolean emailNotificationsEnabled = true;

    @Column(name = "in_app_notifications_enabled", nullable = false)
    @Builder.Default
    private boolean inAppNotificationsEnabled = true;

    @Column(name = "notify_high_match_jobs", nullable = false)
    @Builder.Default
    private boolean notifyHighMatchJobs = true;

    @Column(name = "high_match_min_score", nullable = false)
    @Builder.Default
    private int highMatchMinScore = 85;

    @Column(name = "notify_interventions", nullable = false)
    @Builder.Default
    private boolean notifyInterventions = true;

    @Column(name = "notify_submissions", nullable = false)
    @Builder.Default
    private boolean notifySubmissions = true;

    @Column(name = "notify_failures", nullable = false)
    @Builder.Default
    private boolean notifyFailures = true;

    @Column(name = "notify_interviews", nullable = false)
    @Builder.Default
    private boolean notifyInterviews = true;

    @Column(name = "notify_deadlines", nullable = false)
    @Builder.Default
    private boolean notifyDeadlines = true;

    @Column(name = "digest_frequency", nullable = false)
    @Builder.Default
    private String digestFrequency = "INSTANT"; // INSTANT, DAILY, WEEKLY
}
