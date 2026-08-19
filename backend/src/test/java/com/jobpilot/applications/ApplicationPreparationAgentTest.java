package com.jobpilot.applications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.dto.ApplicationDto;
import com.jobpilot.applications.dto.UpdateApplicationContentRequest;
import com.jobpilot.applications.service.ApplicationPreparationAgentService;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationPreparationAgentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationPreparationAgentService prepAgentService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    private String authToken;
    private String userEmail;
    private Job testJob;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "app.prep." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Jane Developer")
                .phone("+91 9988776655")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        authToken = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        JobSource source = jobSourceRepository.findByName("MOCK_DISCOVERY_ENGINE")
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name("MOCK_DISCOVERY_ENGINE")
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .build()));

        testJob = jobRepository.save(Job.builder()
                .source(source)
                .externalId("prep-test-" + System.currentTimeMillis())
                .title("Senior Spring Boot Developer")
                .company("Apex Global Tech")
                .location("Bengaluru, India")
                .workMode("HYBRID")
                .salaryMin(BigDecimal.valueOf(1400000))
                .salaryMax(BigDecimal.valueOf(2000000))
                .salaryCurrency("INR")
                .salaryDisplay("₹14-20 LPA")
                .description("Build microservices using Java 21, Spring Boot 3, and PostgreSQL.")
                .requiredSkills("[\"Java\", \"Spring Boot\", \"PostgreSQL\"]")
                .postedAt(Instant.now())
                .build());
    }

    @Test
    void testApplicationPreparationWithZeroFabricationGuardrail() {
        ApplicationDto app = prepAgentService.prepareApplication(userEmail, testJob.getId());

        assertNotNull(app);
        assertNotNull(app.getId());
        assertEquals("Apex Global Tech", app.getCompany());
        assertNotNull(app.getTailoredResumeContent());
        assertNotNull(app.getCoverLetter());
        assertTrue(app.getCoverLetter().contains("Apex Global Tech"));
        assertTrue(app.getCoverLetter().contains("Senior Spring Boot Developer"));

        // Check screening questions and zero-fabrication guardrail
        assertNotNull(app.getScreeningQuestions());
        assertFalse(app.getScreeningQuestions().isEmpty());

        ApplicationDto.ScreeningQuestionDto unknownQuestion = app.getScreeningQuestions().stream()
                .filter(q -> "REQUIRES_USER_INPUT".equalsIgnoreCase(q.getStatus()) || "UNKNOWN".equalsIgnoreCase(q.getConfidence()))
                .findFirst()
                .orElse(null);

        assertNotNull(unknownQuestion, "Unknown questions must be marked REQUIRES_USER_INPUT");
        assertNull(unknownQuestion.getAiAnswer(), "AI must never invent answers to unknown screening questions");
        assertEquals("REQUIRES_USER_INPUT", app.getPreparationState());

        // Test editing and candidate review approval
        UpdateApplicationContentRequest updateReq = UpdateApplicationContentRequest.builder()
                .coverLetter(app.getCoverLetter() + "\n\nPS: Excited about your mission.")
                .userApproved(true)
                .screeningAnswers(List.of(
                        UpdateApplicationContentRequest.ScreeningAnswerUpdate.builder()
                                .questionId(unknownQuestion.getId())
                                .candidateAnswer("No government clearance required; Indian citizen eligible to work without sponsorship.")
                                .status("ACCEPTED")
                                .build()
                ))
                .build();

        ApplicationDto updated = prepAgentService.updateApplicationContent(userEmail, app.getId(), updateReq);
        assertNotNull(updated);
        assertEquals("USER_APPROVED", updated.getPreparationState());
        assertTrue(updated.getCoverLetter().contains("PS: Excited about your mission."));
    }

    @Test
    void testApplicationPreparationRestFlow() throws Exception {
        // 1. Prepare application: POST /api/applications/prepare/{jobId}
        MvcResult prepResult = mockMvc.perform(post("/api/applications/prepare/" + testJob.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preparationState").value("REQUIRES_USER_INPUT"))
                .andExpect(jsonPath("$.data.coverLetter").isNotEmpty())
                .andReturn();

        String appId = objectMapper.readTree(prepResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Update content: PUT /api/applications/{id}/content
        UpdateApplicationContentRequest updateReq = UpdateApplicationContentRequest.builder()
                .userApproved(true)
                .build();

        mockMvc.perform(put("/api/applications/" + appId + "/content")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preparationState").value("USER_APPROVED"));

        // 3. Submit application: POST /api/applications/{id}/submit
        mockMvc.perform(post("/api/applications/" + appId + "/submit")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }
}
