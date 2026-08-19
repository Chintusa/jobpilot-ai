package com.jobpilot.autoapply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.autoapply.dto.AutoApplyEvaluationResultDto;
import com.jobpilot.autoapply.dto.AutoApplyPolicyDto;
import com.jobpilot.autoapply.dto.UpdateAutoApplyPolicyRequest;
import com.jobpilot.autoapply.entity.AutoApplyDecision;
import com.jobpilot.autoapply.entity.AutoApplyPolicy;
import com.jobpilot.autoapply.repository.AutoApplyDecisionRepository;
import com.jobpilot.autoapply.repository.AutoApplyPolicyRepository;
import com.jobpilot.autoapply.service.AutoApplyService;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.service.AuthService;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AutoApplyPolicyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutoApplyService autoApplyService;

    @Autowired
    private AutoApplyPolicyRepository policyRepository;

    @Autowired
    private AutoApplyDecisionRepository decisionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JobPreferencesRepository jobPreferencesRepository;

    @Autowired
    private HumanInterventionRepository interventionRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Job testJob;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "autoapply.tester." + System.currentTimeMillis() + "@example.com";
        authService.register(RegisterRequest.builder()
                .email(userEmail)
                .password("SecurePass123!")
                .name("Controlled AutoApply Tester")
                .build());

        testUser = userRepository.findByEmail(userEmail).orElseThrow();

        // Create active resume and approved candidate profile
        resumeRepository.save(Resume.builder()
                .user(testUser)
                .fileName("Candidate_Resume.pdf")
                .fileUrl("https://storage.jobpilot.ai/test-resume.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .parsedText("Senior Java Developer with Spring Boot, Microservices, REST APIs, PostgreSQL, Redis, Docker, and AWS experience.")
                .active(true)
                .status("PARSED")
                .build());

        candidateProfileRepository.save(CandidateProfile.builder()
                .user(testUser)
                .currentTitle("Senior Java Backend Engineer")
                .totalExperienceYears(BigDecimal.valueOf(5.0))
                .location("Bengaluru, India")
                .isApproved(true)
                .completenessScore(100)
                .build());

        jobPreferencesRepository.save(JobPreferences.builder()
                .user(testUser)
                .targetRoles("[\"Senior Java Developer\", \"Java Backend Engineer\"]")
                .locations("[\"Bengaluru, India\", \"Remote\"]")
                .workModes("[\"REMOTE\", \"HYBRID\"]")
                .excludedCompanies("[\"BlacklistedCorp\"]")
                .excludedKeywords("[\"Unpaid\", \"Intern\"]")
                .autoApplyEnabled(false)
                .autoApplyMinScore(85)
                .autoApplyDailyLimit(5)
                .requireApproval(true)
                .build());

        testJob = jobRepository.save(Job.builder()
                .title("Senior Java Backend Engineer")
                .company("CloudTech Innovations")
                .location("Bengaluru, India")
                .workMode("HYBRID")
                .experienceMin(BigDecimal.valueOf(3.0))
                .requiredSkills("[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Microservices\"]")
                .salaryMin(BigDecimal.valueOf(1200000))
                .salaryMax(BigDecimal.valueOf(2200000))
                .description("We are seeking a high-caliber Senior Java Backend Engineer.")
                .build());
    }

    @Test
    @DisplayName("Default AutoApplyPolicy values must be enabled=false, minimumScore=85, requireApproval=true, maxApplicationsPerDay=5")
    void testDefaultAutoApplyPolicyValues() {
        AutoApplyPolicyDto policy = autoApplyService.getPolicy(userEmail);

        assertThat(policy).isNotNull();
        assertThat(policy.isEnabled()).isFalse();
        assertThat(policy.getMinimumScore()).isEqualTo(85);
        assertThat(policy.isRequireApproval()).isTrue();
        assertThat(policy.getMaxApplicationsPerDay()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deterministic evaluation: Fails with POLICY_DISABLED when enabled is false")
    void testEvaluationFailsWhenPolicyDisabled() {
        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userEmail, testJob.getId());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDecision()).isEqualTo("REJECTED_POLICY_DISABLED");
        assertThat(result.getFailedConditions()).anyMatch(c -> c.contains("POLICY_DISABLED"));
        assertThat(result.getPassedConditions()).anyMatch(c -> c.contains("SCORE_THRESHOLD_MET") || c.contains("RESUME_AND_PROFILE_READY"));

        // Verify decision audit log record exists
        List<AutoApplyDecision> decisions = decisionRepository.findByUserIdOrderByTimestampDesc(testUser.getId());
        assertThat(decisions).hasSize(1);
        AutoApplyDecision decision = decisions.get(0);
        assertThat(decision.getScore()).isEqualTo(result.getScore());
        assertThat(decision.getPolicyValues()).contains("\"enabled\":false");
        assertThat(decision.getPolicyValues()).contains("\"minimumScore\":85");
        assertThat(decision.getPolicyValues()).contains("\"requireApproval\":true");
        assertThat(decision.getPolicyValues()).contains("\"maxApplicationsPerDay\":5");
        assertThat(decision.getPassedConditions()).contains("RESUME_AND_PROFILE_READY");
        assertThat(decision.getFailedConditions()).contains("POLICY_DISABLED");
        assertThat(decision.getDecision()).isEqualTo("REJECTED_POLICY_DISABLED");
        assertThat(decision.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deterministic evaluation: Fails with SCORE_BELOW_MINIMUM when score < minimumScore")
    void testEvaluationFailsWhenScoreBelowMinimum() {
        // Enable policy and set high minimumScore (100)
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(100)
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userEmail, testJob.getId());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDecision()).isEqualTo("REJECTED_LOW_SCORE");
        assertThat(result.getFailedConditions()).anyMatch(c -> c.contains("SCORE_BELOW_MINIMUM"));
        assertThat(result.getPassedConditions()).anyMatch(c -> c.contains("POLICY_ENABLED"));

        // Verify audit trail
        List<AutoApplyDecision> decisions = decisionRepository.findByUserIdOrderByTimestampDesc(testUser.getId());
        assertThat(decisions).isNotEmpty();
        AutoApplyDecision decision = decisions.get(0);
        assertThat(decision.getDecision()).isEqualTo("REJECTED_LOW_SCORE");
        assertThat(decision.getFailedConditions()).contains("SCORE_BELOW_MINIMUM");
    }

    @Test
    @DisplayName("Deterministic evaluation: Fails with EXCLUDED_CRITERIA when company is in excluded list")
    void testEvaluationFailsOnExcludedCompany() {
        // Enable policy
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .build());

        Job excludedJob = jobRepository.save(Job.builder()
                .title("Java Developer")
                .company("BlacklistedCorp India")
                .location("Bengaluru, India")
                .workMode("HYBRID")
                .requiredSkills("[\"Java\", \"Spring Boot\"]")
                .description("Java development job at blacklisted firm")
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userEmail, excludedJob.getId());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getFailedConditions()).anyMatch(c -> c.contains("EXCLUDED_CRITERIA"));
    }

    @Test
    @DisplayName("Deterministic evaluation: Fails when daily application quota is exhausted")
    void testEvaluationFailsWhenDailyLimitReached() {
        // Configure policy with daily limit of 2 and enabled=true
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .maxApplicationsPerDay(2)
                .build());

        // Simulate 2 executed decisions today
        decisionRepository.save(AutoApplyDecision.builder()
                .user(testUser)
                .job(testJob)
                .score(90)
                .policyValues("{\"enabled\":true,\"minimumScore\":80,\"requireApproval\":true,\"maxApplicationsPerDay\":2}")
                .passedConditions("[\"POLICY_ENABLED\"]")
                .failedConditions("[]")
                .decision("REQUIRES_USER_APPROVAL")
                .decisionReason("Passed")
                .timestamp(Instant.now())
                .build());

        decisionRepository.save(AutoApplyDecision.builder()
                .user(testUser)
                .job(testJob)
                .score(92)
                .policyValues("{\"enabled\":true,\"minimumScore\":80,\"requireApproval\":true,\"maxApplicationsPerDay\":2}")
                .passedConditions("[\"POLICY_ENABLED\"]")
                .failedConditions("[]")
                .decision("REQUIRES_USER_APPROVAL")
                .decisionReason("Passed")
                .timestamp(Instant.now())
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userEmail, testJob.getId());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDecision()).isEqualTo("REJECTED_DAILY_LIMIT_EXCEEDED");
        assertThat(result.getFailedConditions()).anyMatch(c -> c.contains("DAILY_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Safety Rule: Never bypass CAPTCHA/MFA - stops when security intervention is required")
    void testSecurityInterventionBlocksAutoApply() {
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .build());

        // Create application and pending CAPTCHA intervention
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("PREPARING")
                .preparationState("REQUIRES_USER_INPUT")
                .build());

        interventionRepository.save(HumanIntervention.builder()
                .user(testUser)
                .application(app)
                .reason("CAPTCHA")
                .type("CAPTCHA")
                .description("Portal required CAPTCHA challenge")
                .status("PENDING")
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.evaluateJob(userEmail, testJob.getId());

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getDecision()).isEqualTo("INTERVENTION_REQUIRED");
        assertThat(result.getFailedConditions()).anyMatch(c -> c.contains("SECURITY_INTERVENTION_REQUIRED"));
    }

    @Test
    @DisplayName("Controlled Auto-Apply with default requireApproval=true: Prepares application in READY_FOR_REVIEW and requires user approval before submission")
    void testControlledAutoApplyRequiresApprovalByDefault() {
        // Enable policy with default requireApproval=true
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .requireApproval(true)
                .maxApplicationsPerDay(5)
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.processAutoApply(userEmail, testJob.getId());

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDecision()).isEqualTo("REQUIRES_USER_APPROVAL");
        assertThat(result.getApplicationStatus()).isEqualTo("PENDING_REVIEW");
        assertThat(result.getApplicationPreparationState()).isEqualTo("READY_FOR_REVIEW");

        // Verify application record
        Application app = applicationRepository.findById(result.getApplicationId()).orElseThrow();
        assertThat(app.getStatus()).isEqualTo("PENDING_REVIEW");
        assertThat(app.getPreparationState()).isEqualTo("READY_FOR_REVIEW");
        assertThat(app.getAppliedAt()).isNull(); // Not yet submitted without user approval

        // Verify audit decision log records all 6 required fields
        List<AutoApplyDecision> decisions = decisionRepository.findByUserIdOrderByTimestampDesc(testUser.getId());
        assertThat(decisions).isNotEmpty();
        AutoApplyDecision auditRecord = decisions.get(0);
        assertThat(auditRecord.getScore()).isEqualTo(result.getScore());
        assertThat(auditRecord.getPolicyValues()).contains("\"requireApproval\":true");
        assertThat(auditRecord.getPassedConditions()).contains("POLICY_ENABLED");
        assertThat(auditRecord.getFailedConditions()).isEqualTo("[]");
        assertThat(auditRecord.getDecision()).isEqualTo("REQUIRES_USER_APPROVAL");
        assertThat(auditRecord.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Safety Rule: Controlled Auto-Apply when question is unknown halts for human intervention to prevent fabrication")
    void testControlledAutoApplyHaltsOnUnknownScreeningQuestion() {
        // Enable policy with requireApproval=false
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .requireApproval(false)
                .maxApplicationsPerDay(5)
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.processAutoApply(userEmail, testJob.getId());

        assertThat(result.isEligible()).isTrue();
        // Since default preparation creates an unknown clearance question, zero-fabrication safety rule triggers INTERVENTION_REQUIRED
        assertThat(result.getDecision()).isEqualTo("INTERVENTION_REQUIRED");
        assertThat(result.getApplicationStatus()).isEqualTo("PREPARING");
        assertThat(result.getApplicationPreparationState()).isEqualTo("REQUIRES_USER_INPUT");
    }

    @Test
    @DisplayName("Controlled Auto-Apply when candidate explicitly disables requireApproval and all questions are verified: Submits application directly")
    void testControlledAutoApplyExplicitDirectSubmissionWhenQuestionsVerified() {
        // Pre-prepare application with all verified/accepted questions
        Application app = applicationRepository.save(Application.builder()
                .user(testUser)
                .job(testJob)
                .status("PREPARING")
                .preparationState("READY_FOR_REVIEW")
                .build());

        app.getScreeningQuestions().add(com.jobpilot.applications.entity.ScreeningQuestion.builder()
                .application(app)
                .question("Years of experience?")
                .aiAnswer("5 years")
                .confidence("HIGH")
                .status("ACCEPTED")
                .build());
        applicationRepository.save(app);

        // Enable policy with explicit requireApproval=false
        autoApplyService.updatePolicy(userEmail, UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(80)
                .requireApproval(false)
                .maxApplicationsPerDay(5)
                .build());

        AutoApplyEvaluationResultDto result = autoApplyService.processAutoApply(userEmail, testJob.getId());

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getDecision()).isEqualTo("AUTO_APPLY_EXECUTED");
        assertThat(result.getApplicationStatus()).isEqualTo("SUBMITTED");

        // Verify application was submitted and applied timestamp recorded
        Application submittedApp = applicationRepository.findById(result.getApplicationId()).orElseThrow();
        assertThat(submittedApp.getStatus()).isEqualTo("SUBMITTED");
        assertThat(submittedApp.getAppliedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "autoapply.api.tester@example.com")
    @DisplayName("REST API: Policy CRUD, Evaluate, Process, and Decisions endpoints")
    void testAutoApplyRestEndpoints() throws Exception {
        String apiUserEmail = "autoapply.api.tester@example.com";
        authService.register(RegisterRequest.builder()
                .email(apiUserEmail)
                .password("SecurePass123!")
                .name("API Tester")
                .build());

        // 1. GET Policy
        mockMvc.perform(get("/api/v1/auto-apply/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.minimumScore").value(85))
                .andExpect(jsonPath("$.data.requireApproval").value(true))
                .andExpect(jsonPath("$.data.maxApplicationsPerDay").value(5));

        // 2. PUT Policy
        UpdateAutoApplyPolicyRequest updateReq = UpdateAutoApplyPolicyRequest.builder()
                .enabled(true)
                .minimumScore(88)
                .requireApproval(true)
                .maxApplicationsPerDay(10)
                .build();

        mockMvc.perform(put("/api/v1/auto-apply/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.minimumScore").value(88))
                .andExpect(jsonPath("$.data.maxApplicationsPerDay").value(10));

        // 3. POST Evaluate Job
        mockMvc.perform(post("/api/v1/auto-apply/evaluate/" + testJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").isNumber())
                .andExpect(jsonPath("$.data.passedConditions").isArray())
                .andExpect(jsonPath("$.data.decision").isNotEmpty())
                .andExpect(jsonPath("$.data.timestamp").isNotEmpty());

        // 4. GET Decisions history
        mockMvc.perform(get("/api/v1/auto-apply/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].score").isNumber())
                .andExpect(jsonPath("$.data[0].policyValues").isMap())
                .andExpect(jsonPath("$.data[0].passedConditions").isArray())
                .andExpect(jsonPath("$.data[0].failedConditions").isArray())
                .andExpect(jsonPath("$.data[0].decision").isNotEmpty())
                .andExpect(jsonPath("$.data[0].timestamp").isNotEmpty());
    }
}
