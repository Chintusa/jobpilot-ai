package com.jobpilot.applications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.dto.ApplicationDto;
import com.jobpilot.applications.dto.ApplicationEventDto;
import com.jobpilot.applications.dto.ApplicationStatisticsDto;
import com.jobpilot.applications.dto.UpdateApplicationStatusRequest;
import com.jobpilot.applications.dto.WorkerCallbackRequest;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ApplicationEvent;
import com.jobpilot.applications.entity.ApplicationStatus;
import com.jobpilot.applications.repository.ApplicationEventRepository;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.applications.service.ApplicationService;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.service.AuthService;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
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
public class ApplicationTrackingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationEventRepository eventRepository;

    @Autowired
    private HumanInterventionRepository interventionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Job testJob1;
    private Job testJob2;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "tracking.tester." + System.currentTimeMillis() + "@example.com";
        authService.register(RegisterRequest.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .name("Tracking Lifecycle Tester")
                .build());

        testUser = userRepository.findByEmail(userEmail).orElseThrow();

        JobSource source = jobSourceRepository.findByName("LINKEDIN_PORTAL")
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name("LINKEDIN_PORTAL")
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .build()));

        testJob1 = jobRepository.save(Job.builder()
                .source(source)
                .title("Senior Distributed Systems Engineer")
                .company("FinTech ScaleUp")
                .location("Bengaluru, India")
                .workMode("HYBRID")
                .experienceMin(BigDecimal.valueOf(4.0))
                .requiredSkills("[\"Java\", \"Spring Boot\", \"Kafka\"]")
                .description("Build real-time payment processors.")
                .build());

        testJob2 = jobRepository.save(Job.builder()
                .source(source)
                .title("Full Stack Cloud Architect")
                .company("CloudNative Systems")
                .location("Remote")
                .workMode("REMOTE")
                .experienceMin(BigDecimal.valueOf(5.0))
                .requiredSkills("[\"Java\", \"React\", \"AWS\"]")
                .description("Architect multi-tenant cloud platforms.")
                .build());
    }

    @Test
    @DisplayName("Application Lifecycle: Record complete event timeline with status, source, job, and user actions")
    void testCompleteLifecycleTimeline() {
        // 1. Initialize application
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob1)
                .status("PREPARING")
                .preparationState("PREPARING")
                .sourceName("LINKEDIN_PORTAL")
                .build());

        app.addEvent("APPLICATION_INITIALIZED", "DRAFT", "PREPARING", "AI_AGENT",
                "AI Agent started preparation for FinTech ScaleUp", "{\"jobId\":\"" + testJob1.getId() + "\"}");
        applicationRepository.save(app);

        // 2. Transition to READY_FOR_REVIEW
        applicationService.updateApplicationStatus(userEmail, app.getId(), UpdateApplicationStatusRequest.builder()
                .status("READY_FOR_REVIEW")
                .note("Assets prepared: Tailored resume and cover letter generated")
                .source("AI_AGENT")
                .build());

        // 3. Candidate reviews & approves
        applicationService.updateApplicationStatus(userEmail, app.getId(), UpdateApplicationStatusRequest.builder()
                .status("APPROVED")
                .note("Candidate reviewed tailored answers and gave explicit submission consent")
                .source("CANDIDATE_USER")
                .build());

        // 4. Submit
        applicationService.updateApplicationStatus(userEmail, app.getId(), UpdateApplicationStatusRequest.builder()
                .status("SUBMITTED")
                .note("Application successfully dispatched to employer portal")
                .source("APPLICATION_WORKER")
                .metadata(Map.of("confirmationId", "CONF-89123"))
                .build());

        // 5. Candidate invited to interview
        applicationService.updateApplicationStatus(userEmail, app.getId(), UpdateApplicationStatusRequest.builder()
                .status("INTERVIEW")
                .note("Recruiter reached out for System Design round")
                .source("CANDIDATE_USER")
                .build());

        // 6. Offer received
        applicationService.updateApplicationStatus(userEmail, app.getId(), UpdateApplicationStatusRequest.builder()
                .status("OFFER")
                .note("Offer received: ₹24 LPA base + stock options")
                .source("CANDIDATE_USER")
                .build());

        // Verify Application state
        Application updated = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("OFFER");
        assertThat(updated.getAppliedAt()).isNotNull();
        assertThat(updated.getInterviewAt()).isNotNull();
        assertThat(updated.getOfferAt()).isNotNull();

        // Verify complete chronological timeline
        List<ApplicationEventDto> timeline = applicationService.getApplicationTimeline(userEmail, app.getId());
        assertThat(timeline).hasSize(6);

        assertThat(timeline.get(0).getEventType()).isEqualTo("APPLICATION_INITIALIZED");
        assertThat(timeline.get(0).getToStatus()).isEqualTo("PREPARING");

        assertThat(timeline.get(1).getToStatus()).isEqualTo("READY_FOR_REVIEW");

        assertThat(timeline.get(2).getToStatus()).isEqualTo("APPROVED");
        assertThat(timeline.get(2).getSource()).isEqualTo("CANDIDATE_USER");

        assertThat(timeline.get(3).getToStatus()).isEqualTo("SUBMITTED");
        assertThat(timeline.get(3).getMetadata()).containsEntry("confirmationId", "CONF-89123");

        assertThat(timeline.get(4).getToStatus()).isEqualTo("INTERVIEW");

        assertThat(timeline.get(5).getToStatus()).isEqualTo("OFFER");
    }

    @Test
    @DisplayName("Application Worker Callback: Successful submission records SUBMISSION_SUCCESS timeline event")
    void testWorkerCallbackSuccess() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob1)
                .status("SUBMITTING")
                .preparationState("USER_APPROVED")
                .build());

        WorkerCallbackRequest callback = WorkerCallbackRequest.builder()
                .status("SUBMITTED")
                .submissionResult("{\"confirmationNumber\":\"APP-2026-9901\",\"portalStatus\":\"SUCCESS\"}")
                .screenshots(List.of("/screenshots/confirmation_app_1.png"))
                .logs(List.of("Loaded portal", "Filled form", "Submitted"))
                .executionTimeMs(1420L)
                .build();

        ApplicationDto result = applicationService.recordWorkerResult(userEmail, app.getId(), callback);

        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        assertThat(result.getAppliedAt()).isNotNull();
        assertThat(result.getSubmissionResult()).contains("APP-2026-9901");

        List<ApplicationEventDto> timeline = applicationService.getApplicationTimeline(userEmail, app.getId());
        assertThat(timeline).hasSize(1);
        assertThat(timeline.get(0).getEventType()).isEqualTo("SUBMISSION_SUCCESS");
        assertThat(timeline.get(0).getSource()).isEqualTo("APPLICATION_WORKER");
        assertThat(timeline.get(0).getMetadata()).containsKey("screenshots");
    }

    @Test
    @DisplayName("Application Worker Callback: CAPTCHA / MFA detection triggers human intervention and pauses workflow")
    void testWorkerCallbackInterventionRequired() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob1)
                .status("SUBMITTING")
                .build());

        WorkerCallbackRequest callback = WorkerCallbackRequest.builder()
                .status("HUMAN_INTERVENTION_REQUIRED")
                .interventionReason("CAPTCHA")
                .interventionDescription("Employer portal presented Cloudflare Turnstile challenge")
                .screenshots(List.of("/screenshots/captcha_challenge.png"))
                .executionTimeMs(850L)
                .build();

        ApplicationDto result = applicationService.recordWorkerResult(userEmail, app.getId(), callback);

        assertThat(result.getStatus()).isEqualTo("PREPARING");
        assertThat(result.getPreparationState()).isEqualTo("REQUIRES_USER_INPUT");

        // Verify HumanIntervention entity created
        List<HumanIntervention> interventions = interventionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(testUser.getId(), "PENDING");
        assertThat(interventions).hasSize(1);
        assertThat(interventions.get(0).getReason()).isEqualTo("CAPTCHA");

        // Verify timeline event
        List<ApplicationEventDto> timeline = applicationService.getApplicationTimeline(userEmail, app.getId());
        assertThat(timeline.get(0).getEventType()).isEqualTo("HUMAN_INTERVENTION_TRIGGERED");
    }

    @Test
    @DisplayName("Application Worker Callback: Failure records failureReason and SUBMISSION_FAILED timeline event")
    void testWorkerCallbackFailure() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob1)
                .status("SUBMITTING")
                .build());

        WorkerCallbackRequest callback = WorkerCallbackRequest.builder()
                .status("FAILED")
                .failureReason("500 Internal Server Error returned by ATS API endpoint")
                .logs(List.of("Error: Connection reset by peer"))
                .executionTimeMs(3200L)
                .build();

        ApplicationDto result = applicationService.recordWorkerResult(userEmail, app.getId(), callback);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureReason()).contains("500 Internal Server Error");

        List<ApplicationEventDto> timeline = applicationService.getApplicationTimeline(userEmail, app.getId());
        assertThat(timeline.get(0).getEventType()).isEqualTo("SUBMISSION_FAILED");
    }

    @Test
    @DisplayName("Statistics & Funnel Metrics: Accurately compute conversion rates, totals, and breakdowns")
    void testApplicationStatistics() {
        // Seed multiple applications in different states
        applicationRepository.save(Application.builder().user(testUser).job(testJob1).status("SUBMITTED").sourceName("LINKEDIN_PORTAL").appliedAt(Instant.now()).build());
        applicationRepository.save(Application.builder().user(testUser).job(testJob2).status("INTERVIEW").sourceName("LINKEDIN_PORTAL").appliedAt(Instant.now()).interviewAt(Instant.now()).build());
        applicationRepository.save(Application.builder().user(testUser).job(testJob1).status("OFFER").sourceName("INDEED_PORTAL").appliedAt(Instant.now()).offerAt(Instant.now()).build());
        applicationRepository.save(Application.builder().user(testUser).job(testJob2).status("REJECTED").sourceName("INDEED_PORTAL").appliedAt(Instant.now()).rejectedAt(Instant.now()).build());
        applicationRepository.save(Application.builder().user(testUser).job(testJob1).status("PREPARING").sourceName("JOBPILOT_DIRECT").build());

        ApplicationStatisticsDto stats = applicationService.getApplicationStatistics(userEmail);

        assertThat(stats.getTotalApplications()).isEqualTo(5);
        assertThat(stats.getSubmittedCount()).isEqualTo(1);
        assertThat(stats.getInterviewCount()).isEqualTo(1);
        assertThat(stats.getOfferCount()).isEqualTo(1);
        assertThat(stats.getRejectedCount()).isEqualTo(1);
        assertThat(stats.getPreparingCount()).isEqualTo(1);

        // Active submitted funnel = 1 submitted + 1 interview + 1 offer = 3
        // Interview rate = (1 interview + 1 offer) / 3 = 66.7%
        assertThat(stats.getInterviewRate()).isGreaterThan(60.0);
        // Offer rate = 1 / 3 = 33.3%
        assertThat(stats.getOfferRate()).isGreaterThan(30.0);

        assertThat(stats.getStatusBreakdown()).containsKeys("SUBMITTED", "INTERVIEW", "OFFER", "REJECTED", "PREPARING");
        assertThat(stats.getSourceBreakdown()).containsKeys("LINKEDIN_PORTAL", "INDEED_PORTAL", "JOBPILOT_DIRECT");
    }

    @Test
    @WithMockUser(username = "tracking.api.user@example.com")
    @DisplayName("REST API: Verify Application Tracking, timeline, filter, and statistics endpoints")
    void testApplicationTrackingRestEndpoints() throws Exception {
        String apiUserEmail = "tracking.api.user@example.com";
        authService.register(RegisterRequest.builder()
                .email(apiUserEmail)
                .password("SecurePass123!")
                .name("Tracking API User")
                .build());

        User apiUser = userRepository.findByEmail(apiUserEmail).orElseThrow();

        Application app = applicationRepository.save(Application.builder()
                .user(apiUser)
                .job(testJob1)
                .status("PREPARING")
                .preparationState("PREPARING")
                .sourceName("LINKEDIN_PORTAL")
                .build());

        app.addEvent("APPLICATION_INITIALIZED", "DRAFT", "PREPARING", "AI_AGENT", "Initiated preparation", "{}");
        applicationRepository.save(app);

        // 1. GET /api/v1/applications
        mockMvc.perform(get("/api/v1/applications?status=PREPARING&company=FinTech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].company").value("FinTech ScaleUp"))
                .andExpect(jsonPath("$.data[0].status").value("PREPARING"));

        // 2. GET /api/v1/applications/{id}
        mockMvc.perform(get("/api/v1/applications/" + app.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(app.getId().toString()))
                .andExpect(jsonPath("$.data.jobTitle").value("Senior Distributed Systems Engineer"));

        // 3. GET /api/v1/applications/{id}/timeline
        mockMvc.perform(get("/api/v1/applications/" + app.getId() + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("APPLICATION_INITIALIZED"))
                .andExpect(jsonPath("$.data[0].toStatus").value("PREPARING"));

        // 4. PUT /api/v1/applications/{id}/status
        UpdateApplicationStatusRequest statusReq = UpdateApplicationStatusRequest.builder()
                .status("INTERVIEW")
                .note("Cleared initial technical screen")
                .source("CANDIDATE_USER")
                .build();

        mockMvc.perform(put("/api/v1/applications/" + app.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INTERVIEW"));

        // 5. GET /api/v1/applications/statistics
        mockMvc.perform(get("/api/v1/applications/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalApplications").isNumber())
                .andExpect(jsonPath("$.data.statusBreakdown").isMap());
    }
}
