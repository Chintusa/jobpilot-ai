package com.jobpilot.notifications.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Fired when the AI Recruiter Matching Engine identifies a job with a score >= user's configured threshold.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighMatchJobEvent {
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID jobId;
    private String jobTitle;
    private String company;
    private String location;
    private int matchScore;
    private String matchVerdict;
}
