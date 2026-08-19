package com.jobpilot.notifications.entity;

/**
 * All supported notification event categories.
 * Used for preference filtering and deduplication.
 */
public enum NotificationCategory {
    HIGH_MATCH_JOB,
    HUMAN_INTERVENTION,
    APPLICATION_SUBMITTED,
    APPLICATION_FAILED,
    INTERVIEW_SCHEDULED,
    DEADLINE_APPROACHING,
    STATUS_CHANGE,
    GENERAL;

    public static NotificationCategory fromString(String val) {
        if (val == null || val.isBlank()) return GENERAL;
        try {
            return NotificationCategory.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}
