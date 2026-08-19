package com.jobpilot.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.service.AuthService;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.notifications.dto.*;
import com.jobpilot.notifications.entity.Notification;
import com.jobpilot.notifications.entity.NotificationCategory;
import com.jobpilot.notifications.entity.NotificationPreferences;
import com.jobpilot.notifications.event.*;
import com.jobpilot.notifications.provider.MockEmailNotificationProvider;
import com.jobpilot.notifications.repository.NotificationPreferencesRepository;
import com.jobpilot.notifications.repository.NotificationRepository;
import com.jobpilot.notifications.service.NotificationEventListener;
import com.jobpilot.notifications.service.NotificationService;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationSystemTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationService notificationService;
    @Autowired private NotificationEventListener eventListener;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationPreferencesRepository preferencesRepository;
    @Autowired private MockEmailNotificationProvider mockEmailProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private JobSourceRepository jobSourceRepository;
    @Autowired private AuthService authService;
    @Autowired private ObjectMapper objectMapper;

    private User testUser;
    private String userEmail;
    private Job testJob;

    @BeforeEach
    void setUp() {
        mockEmailProvider.clearSentEmails();

        userEmail = "notif.tester." + System.currentTimeMillis() + "@example.com";
        authService.register(RegisterRequest.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .name("Notification Tester")
                .build());
        testUser = userRepository.findByEmail(userEmail).orElseThrow();

        JobSource source = jobSourceRepository.findByName("LINKEDIN_PORTAL")
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name("LINKEDIN_PORTAL")
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .build()));

        testJob = jobRepository.save(Job.builder()
                .source(source)
                .title("Staff Platform Engineer")
                .company("TechVault Systems")
                .location("Bengaluru, India")
                .workMode("HYBRID")
                .experienceMin(BigDecimal.valueOf(5.0))
                .requiredSkills("[\"Java\", \"Kubernetes\", \"Kafka\"]")
                .description("Build next-gen platform infrastructure.")
                .build());
    }

    @Test
    @DisplayName("Default notification preferences are created automatically with correct defaults")
    void testDefaultPreferencesCreation() {
        NotificationPreferencesDto prefs = notificationService.getPreferences(userEmail);

        assertThat(prefs).isNotNull();
        assertThat(prefs.isEmailNotificationsEnabled()).isTrue();
        assertThat(prefs.isInAppNotificationsEnabled()).isTrue();
        assertThat(prefs.isNotifyHighMatchJobs()).isTrue();
        assertThat(prefs.getHighMatchMinScore()).isEqualTo(85);
        assertThat(prefs.isNotifyInterventions()).isTrue();
        assertThat(prefs.isNotifySubmissions()).isTrue();
        assertThat(prefs.isNotifyFailures()).isTrue();
        assertThat(prefs.isNotifyInterviews()).isTrue();
        assertThat(prefs.isNotifyDeadlines()).isTrue();
        assertThat(prefs.getDigestFrequency()).isEqualTo("INSTANT");
    }

    @Test
    @DisplayName("HighMatchJobEvent: Delivers in-app + email notification when score >= threshold")
    void testHighMatchJobNotification() {
        HighMatchJobEvent event = HighMatchJobEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .jobId(testJob.getId())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .location("Bengaluru, India")
                .matchScore(92)
                .matchVerdict("EXCELLENT")
                .build();

        eventListener.onHighMatchJob(event);

        // Verify in-app notification
        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("HIGH_MATCH_JOB");
        assertThat(notifications.get(0).getType()).isEqualTo("SUCCESS");
        assertThat(notifications.get(0).getTitle()).contains("High-Match Job");
        assertThat(notifications.get(0).getMessage()).contains("92%");
        assertThat(notifications.get(0).getReferenceId()).isEqualTo(testJob.getId());
        assertThat(notifications.get(0).getReferenceType()).isEqualTo("JOB");
        assertThat(notifications.get(0).isRead()).isFalse();

        // Verify email sent
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
        assertThat(mockEmailProvider.getSentEmails().get(0).recipientEmail()).isEqualTo(userEmail);
        assertThat(mockEmailProvider.getSentEmails().get(0).subject()).contains("High-Match Job");
    }

    @Test
    @DisplayName("HighMatchJobEvent: Suppressed when score < user's configured threshold")
    void testHighMatchJobBelowThreshold() {
        HighMatchJobEvent event = HighMatchJobEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .jobId(testJob.getId())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .location("Bengaluru, India")
                .matchScore(70) // Below default threshold of 85
                .matchVerdict("GOOD")
                .build();

        eventListener.onHighMatchJob(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).isEmpty();
        assertThat(mockEmailProvider.getSentEmails()).isEmpty();
    }

    @Test
    @DisplayName("HumanInterventionEvent: Delivers INTERVENTION notification for CAPTCHA/MFA")
    void testHumanInterventionNotification() {
        UUID appId = UUID.randomUUID();
        UUID interventionId = UUID.randomUUID();

        HumanInterventionEvent event = HumanInterventionEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .applicationId(appId)
                .interventionId(interventionId)
                .interventionReason("CAPTCHA")
                .description("Employer portal presented Cloudflare Turnstile challenge")
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .build();

        eventListener.onHumanIntervention(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("HUMAN_INTERVENTION");
        assertThat(notifications.get(0).getType()).isEqualTo("INTERVENTION");
        assertThat(notifications.get(0).getTitle()).contains("Action Required");
        assertThat(notifications.get(0).getTitle()).contains("CAPTCHA");
        assertThat(notifications.get(0).getMessage()).contains("Cloudflare Turnstile");
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("ApplicationSubmittedEvent: Delivers SUCCESS notification")
    void testApplicationSubmittedNotification() {
        UUID appId = UUID.randomUUID();

        ApplicationSubmittedEvent event = ApplicationSubmittedEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .applicationId(appId)
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .submissionMethod("BROWSER_AUTOMATION")
                .build();

        eventListener.onApplicationSubmitted(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("APPLICATION_SUBMITTED");
        assertThat(notifications.get(0).getType()).isEqualTo("SUCCESS");
        assertThat(notifications.get(0).getTitle()).contains("Application Submitted");
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("ApplicationFailedEvent: Delivers WARNING notification with failure reason")
    void testApplicationFailedNotification() {
        UUID appId = UUID.randomUUID();

        ApplicationFailedEvent event = ApplicationFailedEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .applicationId(appId)
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .failureReason("500 Internal Server Error from employer ATS")
                .build();

        eventListener.onApplicationFailed(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("APPLICATION_FAILED");
        assertThat(notifications.get(0).getType()).isEqualTo("WARNING");
        assertThat(notifications.get(0).getMessage()).contains("500 Internal Server Error");
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("InterviewScheduledEvent: Delivers interview notification")
    void testInterviewScheduledNotification() {
        UUID appId = UUID.randomUUID();

        InterviewScheduledEvent event = InterviewScheduledEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .applicationId(appId)
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .interviewAt(Instant.now().plusSeconds(86400))
                .interviewDetails("System Design Round with VP Engineering")
                .build();

        eventListener.onInterviewScheduled(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("INTERVIEW_SCHEDULED");
        assertThat(notifications.get(0).getTitle()).contains("Interview");
        assertThat(notifications.get(0).getMessage()).contains("System Design Round");
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("DeadlineApproachingEvent: Delivers deadline warning notification")
    void testDeadlineApproachingNotification() {
        DeadlineApproachingEvent event = DeadlineApproachingEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .jobId(testJob.getId())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .deadline(Instant.now().plusSeconds(43200))
                .hoursRemaining(12)
                .build();

        eventListener.onDeadlineApproaching(event);

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getCategory()).isEqualTo("DEADLINE_APPROACHING");
        assertThat(notifications.get(0).getType()).isEqualTo("WARNING");
        assertThat(notifications.get(0).getTitle()).contains("12h");
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("Deduplication: Prevents duplicate notifications for the same event within time window")
    void testDeduplicationPrevention() {
        // Fire the same high-match event twice
        HighMatchJobEvent event = HighMatchJobEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .jobId(testJob.getId())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .location("Bengaluru, India")
                .matchScore(95)
                .matchVerdict("EXCELLENT")
                .build();

        eventListener.onHighMatchJob(event);
        eventListener.onHighMatchJob(event); // Duplicate — should be suppressed

        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1); // Only one notification created

        // Email also sent only once
        assertThat(mockEmailProvider.getSentEmails()).hasSize(1);
    }

    @Test
    @DisplayName("Preference Filter: Disabling a category suppresses that event's notifications entirely")
    void testPreferenceFilterSuppression() {
        // Disable high-match job notifications
        notificationService.updatePreferences(userEmail, UpdateNotificationPreferencesRequest.builder()
                .notifyHighMatchJobs(false)
                .build());

        HighMatchJobEvent event = HighMatchJobEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .jobId(testJob.getId())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .location("Bengaluru, India")
                .matchScore(98)
                .matchVerdict("EXCELLENT")
                .build();

        eventListener.onHighMatchJob(event);

        // Should be empty — preference disabled
        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).isEmpty();
        assertThat(mockEmailProvider.getSentEmails()).isEmpty();
    }

    @Test
    @DisplayName("Email Toggle: Disabling email keeps in-app but prevents email delivery")
    void testEmailToggleOff() {
        notificationService.updatePreferences(userEmail, UpdateNotificationPreferencesRequest.builder()
                .emailNotificationsEnabled(false)
                .build());

        ApplicationSubmittedEvent event = ApplicationSubmittedEvent.builder()
                .userId(testUser.getId())
                .userEmail(userEmail)
                .userName(testUser.getName())
                .applicationId(UUID.randomUUID())
                .jobTitle("Staff Platform Engineer")
                .company("TechVault Systems")
                .submissionMethod("AI_AGENT")
                .build();

        eventListener.onApplicationSubmitted(event);

        // In-app notification still created
        List<NotificationDto> notifications = notificationService.getUserNotifications(userEmail);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getChannel()).isEqualTo("IN_APP");

        // No email sent
        assertThat(mockEmailProvider.getSentEmails()).isEmpty();
    }

    @Test
    @WithMockUser(username = "notif.api.tester@example.com")
    @DisplayName("REST API: Verify all notification endpoints — list, unread-count, read, read-all, preferences")
    void testNotificationRestEndpoints() throws Exception {
        String apiEmail = "notif.api.tester@example.com";
        authService.register(RegisterRequest.builder()
                .email(apiEmail)
                .password("SecurePass123!")
                .name("Notification API Tester")
                .build());

        User apiUser = userRepository.findByEmail(apiEmail).orElseThrow();

        // Seed two notifications
        notificationRepository.save(Notification.builder()
                .user(apiUser).title("Test Alert 1").message("First alert body")
                .type("INFO").category("HIGH_MATCH_JOB").channel("BOTH")
                .dedupKey("TEST:1").build());
        notificationRepository.save(Notification.builder()
                .user(apiUser).title("Test Alert 2").message("Second alert body")
                .type("WARNING").category("APPLICATION_FAILED").channel("IN_APP")
                .dedupKey("TEST:2").build());

        // 1. GET /api/v1/notifications
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        // 2. GET /api/v1/notifications?category=HIGH_MATCH_JOB
        mockMvc.perform(get("/api/v1/notifications?category=HIGH_MATCH_JOB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value("HIGH_MATCH_JOB"));

        // 3. GET /api/v1/notifications/unread-count
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(2));

        // 4. PUT /api/v1/notifications/read-all
        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.markedCount").value(2));

        // Verify unread count is now 0
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        // 5. GET /api/v1/notifications/preferences
        mockMvc.perform(get("/api/v1/notifications/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailNotificationsEnabled").value(true))
                .andExpect(jsonPath("$.data.highMatchMinScore").value(85));

        // 6. PUT /api/v1/notifications/preferences
        UpdateNotificationPreferencesRequest prefReq = UpdateNotificationPreferencesRequest.builder()
                .emailNotificationsEnabled(false)
                .highMatchMinScore(90)
                .notifyDeadlines(false)
                .build();

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emailNotificationsEnabled").value(false))
                .andExpect(jsonPath("$.data.highMatchMinScore").value(90))
                .andExpect(jsonPath("$.data.notifyDeadlines").value(false));
    }
}
