package com.jobpilot.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationPreferencesRequest {
    private Boolean emailNotificationsEnabled;
    private Boolean inAppNotificationsEnabled;
    private Boolean notifyHighMatchJobs;
    private Integer highMatchMinScore;
    private Boolean notifyInterventions;
    private Boolean notifySubmissions;
    private Boolean notifyFailures;
    private Boolean notifyInterviews;
    private Boolean notifyDeadlines;
    private String digestFrequency;
}
