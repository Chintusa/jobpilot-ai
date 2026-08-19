package com.jobpilot.notifications.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when an interview is scheduled or a candidate transitions to INTERVIEW status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScheduledEvent {
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID applicationId;
    private String jobTitle;
    private String company;
    private Instant interviewAt;
    private String interviewDetails;
}
