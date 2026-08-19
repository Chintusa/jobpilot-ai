package com.jobpilot.notifications.service;

import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.notifications.entity.NotificationCategory;
import com.jobpilot.notifications.event.*;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Listens to domain lifecycle events and dispatches notifications
 * through the central NotificationService (which handles preferences, dedup, and delivery).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void onHighMatchJob(HighMatchJobEvent event) {
        log.info("Handling HighMatchJobEvent: jobTitle={}, company={}, score={}, user={}",
                event.getJobTitle(), event.getCompany(), event.getMatchScore(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        // Check against user's configured high-match threshold
        var prefs = notificationService.getOrCreatePreferences(user);
        if (event.getMatchScore() < prefs.getHighMatchMinScore()) {
            log.debug("High-match job below user threshold (score={}, min={}), skipping notification",
                    event.getMatchScore(), prefs.getHighMatchMinScore());
            return;
        }

        String title = "🎯 High-Match Job: " + event.getJobTitle() + " at " + event.getCompany();
        String message = "AI Recruiter found a " + event.getMatchScore() + "% match — "
                + event.getJobTitle() + " at " + event.getCompany()
                + " (" + event.getLocation() + "). Verdict: " + event.getMatchVerdict();

        String dedupKey = "HIGH_MATCH:" + event.getJobId();
        String actionUrl = "/jobs/" + event.getJobId();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("matchScore", event.getMatchScore());
        metadata.put("matchVerdict", event.getMatchVerdict());
        metadata.put("company", event.getCompany());

        notificationService.dispatchNotification(user, NotificationCategory.HIGH_MATCH_JOB, "SUCCESS",
                title, message, actionUrl, event.getJobId(), "JOB", dedupKey, metadata);
    }

    @EventListener
    @Transactional
    public void onHumanIntervention(HumanInterventionEvent event) {
        log.info("Handling HumanInterventionEvent: reason={}, app={}, user={}",
                event.getInterventionReason(), event.getApplicationId(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        String title = "⚠️ Action Required: " + event.getInterventionReason();
        String message = "Your application for " + event.getJobTitle() + " at " + event.getCompany()
                + " needs your attention: " + event.getDescription();

        String dedupKey = "INTERVENTION:" + event.getApplicationId() + ":" + event.getInterventionReason();
        String actionUrl = "/interventions";

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reason", event.getInterventionReason());
        metadata.put("company", event.getCompany());
        metadata.put("interventionId", event.getInterventionId());

        notificationService.dispatchNotification(user, NotificationCategory.HUMAN_INTERVENTION, "INTERVENTION",
                title, message, actionUrl, event.getApplicationId(), "APPLICATION", dedupKey, metadata);
    }

    @EventListener
    @Transactional
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        log.info("Handling ApplicationSubmittedEvent: job={}, company={}, user={}",
                event.getJobTitle(), event.getCompany(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        String title = "✅ Application Submitted: " + event.getJobTitle();
        String message = "Your application for " + event.getJobTitle() + " at " + event.getCompany()
                + " has been successfully submitted via " + event.getSubmissionMethod() + ".";

        String dedupKey = "SUBMITTED:" + event.getApplicationId();
        String actionUrl = "/applications/" + event.getApplicationId();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("company", event.getCompany());
        metadata.put("submissionMethod", event.getSubmissionMethod());

        notificationService.dispatchNotification(user, NotificationCategory.APPLICATION_SUBMITTED, "SUCCESS",
                title, message, actionUrl, event.getApplicationId(), "APPLICATION", dedupKey, metadata);
    }

    @EventListener
    @Transactional
    public void onApplicationFailed(ApplicationFailedEvent event) {
        log.info("Handling ApplicationFailedEvent: job={}, company={}, reason={}, user={}",
                event.getJobTitle(), event.getCompany(), event.getFailureReason(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        String title = "❌ Submission Failed: " + event.getJobTitle();
        String message = "Your application for " + event.getJobTitle() + " at " + event.getCompany()
                + " could not be submitted. Reason: " + event.getFailureReason();

        String dedupKey = "FAILED:" + event.getApplicationId();
        String actionUrl = "/applications/" + event.getApplicationId();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("company", event.getCompany());
        metadata.put("failureReason", event.getFailureReason());

        notificationService.dispatchNotification(user, NotificationCategory.APPLICATION_FAILED, "WARNING",
                title, message, actionUrl, event.getApplicationId(), "APPLICATION", dedupKey, metadata);
    }

    @EventListener
    @Transactional
    public void onInterviewScheduled(InterviewScheduledEvent event) {
        log.info("Handling InterviewScheduledEvent: job={}, company={}, user={}",
                event.getJobTitle(), event.getCompany(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        String title = "📅 Interview: " + event.getJobTitle() + " at " + event.getCompany();
        String details = event.getInterviewDetails() != null ? event.getInterviewDetails() : "Check your email for interview details";
        String message = "You have an interview opportunity for " + event.getJobTitle()
                + " at " + event.getCompany() + ". " + details;

        String dedupKey = "INTERVIEW:" + event.getApplicationId();
        String actionUrl = "/applications/" + event.getApplicationId();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("company", event.getCompany());
        if (event.getInterviewAt() != null) metadata.put("interviewAt", event.getInterviewAt().toString());

        notificationService.dispatchNotification(user, NotificationCategory.INTERVIEW_SCHEDULED, "INFO",
                title, message, actionUrl, event.getApplicationId(), "APPLICATION", dedupKey, metadata);
    }

    @EventListener
    @Transactional
    public void onDeadlineApproaching(DeadlineApproachingEvent event) {
        log.info("Handling DeadlineApproachingEvent: job={}, company={}, hoursRemaining={}, user={}",
                event.getJobTitle(), event.getCompany(), event.getHoursRemaining(), event.getUserEmail());

        User user = resolveUser(event.getUserEmail());

        String title = "⏰ Deadline: " + event.getJobTitle() + " closing in " + event.getHoursRemaining() + "h";
        String message = "The application deadline for " + event.getJobTitle() + " at " + event.getCompany()
                + " is approaching — only " + event.getHoursRemaining() + " hours remaining.";

        String dedupKey = "DEADLINE:" + event.getJobId() + ":" + event.getHoursRemaining();
        String actionUrl = "/jobs/" + event.getJobId();

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("company", event.getCompany());
        metadata.put("hoursRemaining", event.getHoursRemaining());
        if (event.getDeadline() != null) metadata.put("deadline", event.getDeadline().toString());

        notificationService.dispatchNotification(user, NotificationCategory.DEADLINE_APPROACHING, "WARNING",
                title, message, actionUrl, event.getJobId(), "JOB", dedupKey, metadata);
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
