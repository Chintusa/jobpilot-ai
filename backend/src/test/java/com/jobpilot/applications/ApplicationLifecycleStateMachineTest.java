package com.jobpilot.applications;

import com.jobpilot.applications.dto.ApplicationDto;
import com.jobpilot.applications.dto.UpdateApplicationStatusRequest;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ApplicationStatus;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.applications.service.ApplicationService;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.service.AuthService;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApplicationLifecycleStateMachineTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private AuthService authService;

    private User testUser;
    private Job testJob;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "statemachine.tester." + System.currentTimeMillis() + "@example.com";
        authService.register(RegisterRequest.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .name("State Machine Tester")
                .build());

        testUser = userRepository.findByEmail(userEmail).orElseThrow();

        testJob = jobRepository.save(Job.builder()
                .title("Senior Cloud Backend Architect")
                .company("CloudForge AI")
                .location("Remote")
                .workMode("REMOTE")
                .experienceMin(BigDecimal.valueOf(5.0))
                .description("Cloud distributed architectures.")
                .build());
    }

    @Test
    @DisplayName("Main Path: DRAFT -> PREPARING -> READY_FOR_REVIEW -> APPROVED -> SUBMITTING -> SUBMITTED")
    void testMainLifecyclePath() {
        // 1. Initial State: DRAFT
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("DRAFT")
                .preparationState("PREPARING")
                .build());

        assertThat(app.getStatus()).isEqualTo("DRAFT");
        assertThat(ApplicationStatus.DRAFT.canTransitionTo(ApplicationStatus.PREPARING)).isTrue();

        // 2. DRAFT -> PREPARING
        ApplicationDto dto1 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("PREPARING").note("AI preparation started").build());
        assertThat(dto1.getStatus()).isEqualTo("PREPARING");
        assertThat(dto1.getPreparationState()).isEqualTo("PREPARING");
        assertThat(ApplicationStatus.PREPARING.canTransitionTo(ApplicationStatus.READY_FOR_REVIEW)).isTrue();

        // 3. PREPARING -> READY_FOR_REVIEW
        ApplicationDto dto2 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("READY_FOR_REVIEW").note("Tailoring finished").build());
        assertThat(dto2.getStatus()).isEqualTo("READY_FOR_REVIEW");
        assertThat(dto2.getPreparationState()).isEqualTo("READY_FOR_REVIEW");
        assertThat(ApplicationStatus.READY_FOR_REVIEW.canTransitionTo(ApplicationStatus.APPROVED)).isTrue();

        // 4. READY_FOR_REVIEW -> APPROVED
        ApplicationDto dto3 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("APPROVED").note("Candidate approved").build());
        assertThat(dto3.getStatus()).isEqualTo("APPROVED");
        assertThat(dto3.getPreparationState()).isEqualTo("USER_APPROVED");
        assertThat(ApplicationStatus.APPROVED.canTransitionTo(ApplicationStatus.SUBMITTING)).isTrue();

        // 5. APPROVED -> SUBMITTING
        ApplicationDto dto4 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("SUBMITTING").note("Worker dispatched").build());
        assertThat(dto4.getStatus()).isEqualTo("SUBMITTING");
        assertThat(ApplicationStatus.SUBMITTING.canTransitionTo(ApplicationStatus.SUBMITTED)).isTrue();

        // 6. SUBMITTING -> SUBMITTED
        ApplicationDto dto5 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("SUBMITTED").note("Submission confirmed").build());
        assertThat(dto5.getStatus()).isEqualTo("SUBMITTED");
        assertThat(dto5.getPreparationState()).isEqualTo("SUBMITTED");
        assertThat(dto5.getAppliedAt()).isNotNull();
    }

    @Test
    @DisplayName("Alternative Path 1: PREPARING -> REQUIRES_USER_INPUT -> PREPARING")
    void testHumanInterventionAlternativePath() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("PREPARING")
                .preparationState("PREPARING")
                .build());

        // PREPARING -> REQUIRES_USER_INPUT
        assertThat(ApplicationStatus.PREPARING.canTransitionTo(ApplicationStatus.REQUIRES_USER_INPUT)).isTrue();
        ApplicationDto dto1 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("REQUIRES_USER_INPUT").note("Unknown question encountered").build());
        assertThat(dto1.getStatus()).isEqualTo("REQUIRES_USER_INPUT");
        assertThat(dto1.getPreparationState()).isEqualTo("REQUIRES_USER_INPUT");

        // REQUIRES_USER_INPUT -> PREPARING (resumed after candidate input)
        assertThat(ApplicationStatus.REQUIRES_USER_INPUT.canTransitionTo(ApplicationStatus.PREPARING)).isTrue();
        ApplicationDto dto2 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("PREPARING").note("Candidate answered, resuming preparation").build());
        assertThat(dto2.getStatus()).isEqualTo("PREPARING");
        assertThat(dto2.getPreparationState()).isEqualTo("PREPARING");
    }

    @Test
    @DisplayName("Alternative Path 2: SUBMITTING -> FAILED")
    void testFailureAlternativePath() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("SUBMITTING")
                .preparationState("USER_APPROVED")
                .build());

        // SUBMITTING -> FAILED
        assertThat(ApplicationStatus.SUBMITTING.canTransitionTo(ApplicationStatus.FAILED)).isTrue();
        ApplicationDto dto = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("FAILED").note("Portal timed out after 3 retries").build());
        assertThat(dto.getStatus()).isEqualTo("FAILED");
        assertThat(dto.getFailureReason()).isEqualTo("Portal timed out after 3 retries");

        // Can retry: FAILED -> SUBMITTING
        assertThat(ApplicationStatus.FAILED.canTransitionTo(ApplicationStatus.SUBMITTING)).isTrue();
    }

    @Test
    @DisplayName("Alternative Path 3: SUBMITTED -> INTERVIEW -> OFFER")
    void testPostSubmissionOutcomesPath() {
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("SUBMITTED")
                .preparationState("SUBMITTED")
                .build());

        // SUBMITTED -> INTERVIEW
        assertThat(ApplicationStatus.SUBMITTED.canTransitionTo(ApplicationStatus.INTERVIEW)).isTrue();
        ApplicationDto dto1 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("INTERVIEW").note("Screening call scheduled").build());
        assertThat(dto1.getStatus()).isEqualTo("INTERVIEW");
        assertThat(dto1.getInterviewAt()).isNotNull();

        // INTERVIEW -> OFFER
        assertThat(ApplicationStatus.INTERVIEW.canTransitionTo(ApplicationStatus.OFFER)).isTrue();
        ApplicationDto dto2 = applicationService.updateApplicationStatus(userEmail, app.getId(),
                UpdateApplicationStatusRequest.builder().status("OFFER").note("Full-time offer received").build());
        assertThat(dto2.getStatus()).isEqualTo("OFFER");
        assertThat(dto2.getOfferAt()).isNotNull();
    }
}
