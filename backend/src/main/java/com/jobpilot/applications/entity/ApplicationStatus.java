package com.jobpilot.applications.entity;

public enum ApplicationStatus {
    DRAFT,
    PREPARING,
    READY_FOR_REVIEW,
    APPROVED,
    SUBMITTING,
    SUBMITTED,
    FAILED,
    REQUIRES_USER_INPUT,
    WITHDRAWN,
    REJECTED,
    INTERVIEW,
    OFFER;

    public boolean canTransitionTo(ApplicationStatus target) {
        if (target == null) return false;
        if (this == target) return true;

        return switch (this) {
            case DRAFT -> target == PREPARING || target == WITHDRAWN;
            case PREPARING -> target == READY_FOR_REVIEW || target == REQUIRES_USER_INPUT || target == FAILED || target == WITHDRAWN;
            case REQUIRES_USER_INPUT -> target == PREPARING || target == READY_FOR_REVIEW || target == WITHDRAWN;
            case READY_FOR_REVIEW -> target == APPROVED || target == PREPARING || target == REQUIRES_USER_INPUT || target == WITHDRAWN;
            case APPROVED -> target == SUBMITTING || target == READY_FOR_REVIEW || target == WITHDRAWN;
            case SUBMITTING -> target == SUBMITTED || target == FAILED || target == REQUIRES_USER_INPUT || target == WITHDRAWN;
            case FAILED -> target == SUBMITTING || target == PREPARING || target == WITHDRAWN;
            case SUBMITTED -> target == INTERVIEW || target == OFFER || target == REJECTED || target == WITHDRAWN;
            case INTERVIEW -> target == OFFER || target == REJECTED || target == WITHDRAWN;
            case OFFER -> target == REJECTED || target == WITHDRAWN;
            case WITHDRAWN, REJECTED -> false;
        };
    }

    public static ApplicationStatus fromString(String val) {
        if (val == null || val.isBlank()) return DRAFT;
        String normalized = val.trim().toUpperCase();
        try {
            return ApplicationStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Support backward-compatible aliases
            if ("MATCHED".equalsIgnoreCase(val)) return DRAFT;
            if ("PENDING_REVIEW".equalsIgnoreCase(val)) return READY_FOR_REVIEW;
            if ("USER_APPROVED".equalsIgnoreCase(val)) return APPROVED;
            if ("SUCCESS".equalsIgnoreCase(val)) return SUBMITTED;
            return DRAFT;
        }
    }
}
