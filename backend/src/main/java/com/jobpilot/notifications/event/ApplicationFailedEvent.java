package com.jobpilot.notifications.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Fired when an application submission fails at the employer portal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFailedEvent {
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID applicationId;
    private String jobTitle;
    private String company;
    private String failureReason;
}
