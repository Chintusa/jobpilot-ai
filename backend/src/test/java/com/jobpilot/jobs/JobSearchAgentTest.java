package com.jobpilot.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.candidate.dto.UpdateProfileRequest;
import com.jobpilot.jobs.dto.SearchRunDto;
import com.jobpilot.jobs.entity.SearchRun;
import com.jobpilot.jobs.repository.SearchRunRepository;
import com.jobpilot.jobs.service.JobSearchAgentService;
import com.jobpilot.preferences.dto.UpdateJobPreferencesRequest;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobSearchAgentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobSearchAgentService jobSearchAgentService;

    @Autowired
    private SearchRunRepository searchRunRepository;

    private String authToken;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "agent.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Agent Tester")
                .phone("+91 9988776655")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        authToken = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // Configure candidate preferences with exclusions
        UpdateJobPreferencesRequest prefsReq = UpdateJobPreferencesRequest.builder()
                .targetRoles(List.of("Senior Java Developer", "Spring Boot Architect"))
                .roleVariations(List.of("Lead Java Engineer"))
                .locations(List.of("Bengaluru", "Remote"))
                .excludedCompanies(List.of("FinPay Solutions")) // Exclude FinPay
                .autoApplyMinScore(85)
                .build();

        mockMvc.perform(put("/api/preferences")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefsReq)))
                .andExpect(status().isOk());
    }

    @Test
    void testNineStepAutonomousAgentExecution() {
        SearchRunDto run = jobSearchAgentService.executeAutonomousSearchRun(userEmail);

        assertNotNull(run);
        assertEquals("COMPLETED", run.getStatus());
        assertTrue(run.getNumberFound() > 0, "Agent must discover jobs from active sources");
        assertTrue(run.getFilteredJobs() >= 1, "Agent must filter out excluded employers at step 6");
        assertTrue(run.getDurationMs() >= 0);
        assertNotNull(run.getAuditLog());
        assertTrue(run.getAuditLog().contains("[Step 1: Strategies]"));
        assertTrue(run.getAuditLog().contains("[Step 2: Role Variations]"));
        assertTrue(run.getAuditLog().contains("[Step 3: Source Search]"));
        assertTrue(run.getAuditLog().contains("[Step 6: Filtered]"));
        assertTrue(run.getAuditLog().contains("[Step 7 & 8: Scoring & Ranking]"));
        assertTrue(run.getAuditLog().contains("[Step 9: Recommendations]"));

        // Verify SearchRun entity is persisted in PostgreSQL
        List<SearchRun> runs = searchRunRepository.findByUserIdOrderByStartedAtDesc(run.getId());
        assertNotNull(runs);
    }

    @Test
    void testAgentRestEndpoints() throws Exception {
        // 1. Trigger agent run via API: POST /api/agent/search-runs/start
        MvcResult startResult = mockMvc.perform(post("/api/agent/search-runs/start")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.auditLog").isNotEmpty())
                .andReturn();

        String runId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Fetch history: GET /api/agent/search-runs
        mockMvc.perform(get("/api/agent/search-runs")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(runId));

        // 3. Fetch detailed audit by ID: GET /api/agent/search-runs/{id}
        mockMvc.perform(get("/api/agent/search-runs/" + runId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(runId));

        // 4. Fetch agent status: GET /api/agent/status
        mockMvc.perform(get("/api/agent/status")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }
}
