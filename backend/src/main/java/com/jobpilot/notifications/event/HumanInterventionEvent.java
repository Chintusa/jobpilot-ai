package com.jobpilot.notifications.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Fired when a CAPTCHA, MFA, unknown question, or other security challenge requires human action.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanInterventionEvent {
    private UUID userId;
    private String userEmail;
    private String userName;
    private UUID applicationId;
    private UUID interventionId;
    private String interventionReason; // CAPTCHA, MFA, UNKNOWN_QUESTION, ACCESS_CONTROL
    private String description;
    private String jobTitle;
    private String company;
}
