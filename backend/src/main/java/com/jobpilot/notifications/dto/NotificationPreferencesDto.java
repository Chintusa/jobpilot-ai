package com.jobpilot.notifications.dto;

import com.jobpilot.notifications.entity.NotificationPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDto {
    private UUID id;
    private boolean emailNotificationsEnabled;
    private boolean inAppNotificationsEnabled;
    private boolean notifyHighMatchJobs;
    private int highMatchMinScore;
    private boolean notifyInterventions;
    private boolean notifySubmissions;
    private boolean notifyFailures;
    private boolean notifyInterviews;
    private boolean notifyDeadlines;
    private String digestFrequency;

    public static NotificationPreferencesDto fromEntity(NotificationPreferences p) {
        if (p == null) return null;
        return NotificationPreferencesDto.builder()
                .id(p.getId())
                .emailNotificationsEnabled(p.isEmailNotificationsEnabled())
                .inAppNotificationsEnabled(p.isInAppNotificationsEnabled())
                .notifyHighMatchJobs(p.isNotifyHighMatchJobs())
                .highMatchMinScore(p.getHighMatchMinScore())
                .notifyInterventions(p.isNotifyInterventions())
                .notifySubmissions(p.isNotifySubmissions())
                .notifyFailures(p.isNotifyFailures())
                .notifyInterviews(p.isNotifyInterviews())
                .notifyDeadlines(p.isNotifyDeadlines())
                .digestFrequency(p.getDigestFrequency())
                .build();
    }
}
