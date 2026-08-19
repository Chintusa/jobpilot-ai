package com.jobpilot.automation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.automation.dto.HumanInterventionDto;
import com.jobpilot.automation.dto.ResolveInterventionRequest;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.automation.service.AutomationService;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HumanInterventionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AutomationService automationService;

    @Autowired
    private HumanInterventionRepository interventionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    @Autowired
    private UserRepository userRepository;

    private String authToken;
    private String userEmail;
    private Application testApplication;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "intervention.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Intervention Candidate")
                .phone("+91 9988776655")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        authToken = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        User user = userRepository.findByEmail(userEmail).orElseThrow();

        JobSource source = jobSourceRepository.findByName("MOCK_DISCOVERY_ENGINE")
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name("MOCK_DISCOVERY_ENGINE")
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .build()));

        Job job = jobRepository.save(Job.builder()
                .source(source)
                .externalId("intervention-job-" + System.currentTimeMillis())
                .title("Full Stack Engineer")
                .company("FinTech Corp")
                .location("Bengaluru, India")
                .workMode("REMOTE")
                .salaryMin(BigDecimal.valueOf(1200000))
                .salaryMax(BigDecimal.valueOf(1800000))
                .description("React & Spring Boot Engineer")
                .requiredSkills("[\"Java\", \"React\"]")
                .postedAt(Instant.now())
                .build());

        testApplication = applicationRepository.save(Application.builder()
                .user(user)
                .job(job)
                .status("PREPARING")
                .preparationState("PREPARING")
                .build());
    }

    @Test
    void testHumanInterventionLifecycleAndRules() {
        // 1. Create Human Intervention (e.g. CAPTCHA / MFA trigger)
        HumanInterventionDto intervention = automationService.createIntervention(
                userEmail,
                testApplication.getId(),
                "CAPTCHA",
                "CAPTCHA challenge detected on FinTech Corp carrier portal. Never bypass CAPTCHA rule active.",
                "{\"portalUrl\": \"https://fintech.example.com/apply\"}"
        );

        assertNotNull(intervention);
        assertEquals("PENDING", intervention.getStatus());
        assertEquals("CAPTCHA", intervention.getReason());

        // Verify application status changed to REQUIRES_USER_INPUT
        Application app = applicationRepository.findById(testApplication.getId()).orElseThrow();
        assertEquals("REQUIRES_USER_INPUT", app.getPreparationState());

        // 2. Candidate Inspects Context & Resolves (Provides Verification Code)
        ResolveInterventionRequest resolveReq = ResolveInterventionRequest.builder()
                .resolutionPayload("MANUAL_SOLVE_CONFIRMED")
                .action("RESOLVE")
                .build();

        HumanInterventionDto resolved = automationService.resolveIntervention(userEmail, intervention.getId(), resolveReq);
        assertNotNull(resolved);
        assertEquals("RESOLVED", resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // Verify application updated to USER_APPROVED
        Application resumedApp = applicationRepository.findById(testApplication.getId()).orElseThrow();
        assertEquals("USER_APPROVED", resumedApp.getPreparationState());
    }

    @Test
    void testHumanInterventionRestEndpoints() throws Exception {
        // 1. Create a pending intervention
        HumanInterventionDto created = automationService.createIntervention(
                userEmail,
                testApplication.getId(),
                "MFA",
                "MFA OTP Verification required",
                "{\"portal\": \"FinTech Portal\"}"
        );

        // 2. List interventions: GET /api/interventions?status=PENDING
        mockMvc.perform(get("/api/interventions?status=PENDING")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(created.getId().toString()))
                .andExpect(jsonPath("$.data[0].reason").value("MFA"));

        // 3. Inspect context: GET /api/interventions/{id}
        mockMvc.perform(get("/api/interventions/" + created.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        // 4. Resolve & Resume: POST /api/interventions/{id}/resolve
        ResolveInterventionRequest resolveReq = ResolveInterventionRequest.builder()
                .resolutionPayload("849201")
                .build();

        mockMvc.perform(post("/api/interventions/" + created.getId() + "/resolve")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        // 5. Create another and test Cancel: POST /api/interventions/{id}/cancel
        HumanInterventionDto second = automationService.createIntervention(
                userEmail,
                testApplication.getId(),
                "UNKNOWN_QUESTION",
                "Screening question requires answer",
                "{}"
        );

        mockMvc.perform(post("/api/interventions/" + second.getId() + "/cancel")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
