package com.jobpilot.notifications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.notifications.dto.*;
import com.jobpilot.notifications.entity.Notification;
import com.jobpilot.notifications.entity.NotificationCategory;
import com.jobpilot.notifications.entity.NotificationPreferences;
import com.jobpilot.notifications.provider.EmailNotificationProvider;
import com.jobpilot.notifications.repository.NotificationPreferencesRepository;
import com.jobpilot.notifications.repository.NotificationRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final EmailNotificationProvider emailProvider;
    private final ObjectMapper objectMapper;

    /**
     * Deduplication window: suppress duplicate notifications for the same event within this time.
     */
    private static final Duration DEDUP_WINDOW = Duration.ofHours(1);

    // ─── CRUD / Query Operations ─────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userEmail) {
        User user = resolveUser(userEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(String userEmail, String category) {
        User user = resolveUser(userEmail);
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(user.getId(), category.toUpperCase())
                    .stream().map(NotificationDto::fromEntity).collect(Collectors.toList());
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = resolveUser(userEmail);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
            notificationRepository.save(n);
        });
    }

    @Transactional
    public int markAllAsRead(String userEmail) {
        User user = resolveUser(userEmail);
        return notificationRepository.markAllAsRead(user.getId(), Instant.now());
    }

    // ─── Notification Preferences ────────────────────────────

    @Transactional(readOnly = true)
    public NotificationPreferencesDto getPreferences(String userEmail) {
        User user = resolveUser(userEmail);
        NotificationPreferences prefs = getOrCreatePreferences(user);
        return NotificationPreferencesDto.fromEntity(prefs);
    }

    @Transactional
    public NotificationPreferencesDto updatePreferences(String userEmail, UpdateNotificationPreferencesRequest request) {
        User user = resolveUser(userEmail);
        NotificationPreferences prefs = getOrCreatePreferences(user);

        if (request.getEmailNotificationsEnabled() != null) prefs.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        if (request.getInAppNotificationsEnabled() != null) prefs.setInAppNotificationsEnabled(request.getInAppNotificationsEnabled());
        if (request.getNotifyHighMatchJobs() != null) prefs.setNotifyHighMatchJobs(request.getNotifyHighMatchJobs());
        if (request.getHighMatchMinScore() != null) prefs.setHighMatchMinScore(request.getHighMatchMinScore());
        if (request.getNotifyInterventions() != null) prefs.setNotifyInterventions(request.getNotifyInterventions());
        if (request.getNotifySubmissions() != null) prefs.setNotifySubmissions(request.getNotifySubmissions());
        if (request.getNotifyFailures() != null) prefs.setNotifyFailures(request.getNotifyFailures());
        if (request.getNotifyInterviews() != null) prefs.setNotifyInterviews(request.getNotifyInterviews());
        if (request.getNotifyDeadlines() != null) prefs.setNotifyDeadlines(request.getNotifyDeadlines());
        if (request.getDigestFrequency() != null) prefs.setDigestFrequency(request.getDigestFrequency());

        preferencesRepository.save(prefs);
        log.info("Updated notification preferences for user: {}", userEmail);
        return NotificationPreferencesDto.fromEntity(prefs);
    }

    // ─── Event-Driven Notification Dispatch ──────────────────

    /**
     * Central notification dispatch. Evaluates preferences, deduplication, and delivers via appropriate channels.
     *
     * @param user           Target user.
     * @param category       Notification category.
     * @param type           Alert type (INFO, WARNING, SUCCESS, INTERVENTION).
     * @param title          Notification title.
     * @param message        Notification body.
     * @param actionUrl      Optional click-through URL.
     * @param referenceId    Optional entity reference ID (job, application, intervention).
     * @param referenceType  Optional entity type (JOB, APPLICATION, INTERVENTION).
     * @param dedupKey       Dedup key to prevent duplicate notifications for the same event.
     * @param metadata       Additional metadata map.
     * @return The created Notification (or null if suppressed by dedup or preferences).
     */
    @Transactional
    public Notification dispatchNotification(User user, NotificationCategory category, String type,
                                             String title, String message, String actionUrl,
                                             UUID referenceId, String referenceType,
                                             String dedupKey, Map<String, Object> metadata) {

        // 1. Evaluate preferences — is this category enabled?
        NotificationPreferences prefs = getOrCreatePreferences(user);
        if (!isCategoryEnabled(prefs, category)) {
            log.debug("Notification suppressed by preferences: category={}, user={}", category, user.getEmail());
            return null;
        }

        // 2. Deduplication check
        if (dedupKey != null && !dedupKey.isBlank()) {
            Instant since = Instant.now().minus(DEDUP_WINDOW);
            List<Notification> recent = notificationRepository.findRecentByDedupKey(user.getId(), dedupKey, since);
            if (!recent.isEmpty()) {
                log.debug("Notification suppressed by deduplication: dedupKey={}, user={}", dedupKey, user.getEmail());
                return null;
            }
        }

        // 3. Determine channel
        boolean sendEmail = prefs.isEmailNotificationsEnabled();
        boolean sendInApp = prefs.isInAppNotificationsEnabled();
        String channel = (sendEmail && sendInApp) ? "BOTH" : sendEmail ? "EMAIL" : "IN_APP";

        // 4. Create in-app notification
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .category(category.name())
                .dedupKey(dedupKey)
                .channel(channel)
                .actionUrl(actionUrl)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .metadata(toJson(metadata))
                .build();

        if (sendInApp) {
            notification = notificationRepository.save(notification);
            log.info("Created in-app notification: category={}, user={}, title='{}'", category, user.getEmail(), title);
        }

        // 5. Send email if enabled
        if (sendEmail) {
            String emailSubject = "[JobPilot AI] " + title;
            String emailBody = buildEmailBody(title, message, actionUrl);
            boolean delivered = emailProvider.sendEmail(user.getEmail(), user.getName(), emailSubject, emailBody, category.name(), metadata);
            notification.setEmailDelivered(delivered);
            if (sendInApp) {
                notificationRepository.save(notification);
            }
            log.info("Email notification sent: category={}, user={}, delivered={}", category, user.getEmail(), delivered);
        }

        return notification;
    }

    // ─── Preference Evaluation ───────────────────────────────

    private boolean isCategoryEnabled(NotificationPreferences prefs, NotificationCategory category) {
        return switch (category) {
            case HIGH_MATCH_JOB -> prefs.isNotifyHighMatchJobs();
            case HUMAN_INTERVENTION -> prefs.isNotifyInterventions();
            case APPLICATION_SUBMITTED -> prefs.isNotifySubmissions();
            case APPLICATION_FAILED -> prefs.isNotifyFailures();
            case INTERVIEW_SCHEDULED -> prefs.isNotifyInterviews();
            case DEADLINE_APPROACHING -> prefs.isNotifyDeadlines();
            case STATUS_CHANGE, GENERAL -> true; // always enabled
        };
    }

    NotificationPreferences getOrCreatePreferences(User user) {
        return preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    NotificationPreferences defaults = NotificationPreferences.builder()
                            .user(user)
                            .build();
                    return preferencesRepository.save(defaults);
                });
    }

    // ─── Helpers ─────────────────────────────────────────────

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private String buildEmailBody(String title, String message, String actionUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Inter,sans-serif;color:#1a1a2e;'>");
        sb.append("<div style='max-width:600px;margin:0 auto;padding:32px;background:#fafafa;border-radius:12px;'>");
        sb.append("<h2 style='color:#6c5ce7;margin-bottom:16px;'>").append(title).append("</h2>");
        sb.append("<p style='font-size:16px;line-height:1.6;'>").append(message).append("</p>");
        if (actionUrl != null && !actionUrl.isBlank()) {
            sb.append("<a href='").append(actionUrl).append("' style='display:inline-block;margin-top:16px;padding:12px 24px;background:#6c5ce7;color:white;border-radius:8px;text-decoration:none;font-weight:600;'>View Details</a>");
        }
        sb.append("<hr style='margin-top:32px;border:none;border-top:1px solid #e0e0e0;'/>");
        sb.append("<p style='font-size:12px;color:#888;'>JobPilot AI — Your intelligent job search assistant</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String toJson(Object obj) {
        if (obj == null) return "{}";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
