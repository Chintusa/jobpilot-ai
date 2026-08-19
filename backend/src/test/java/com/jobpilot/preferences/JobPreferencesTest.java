package com.jobpilot.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.service.JobDiscoveryService;
import com.jobpilot.jobs.source.JobSearchCriteria;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobPreferencesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobDiscoveryService jobDiscoveryService;

    private String authToken;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "prefs.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Preferences Tester")
                .phone("+91 9988776655")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        authToken = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    @Test
    void testGetAndUpdateJobPreferencesWorkflow() throws Exception {
        // 1. Fetch default preferences: GET /api/preferences
        mockMvc.perform(get("/api/preferences")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetRoles").isArray())
                .andExpect(jsonPath("$.data.autoApplyMinScore").isNumber());

        // 2. Update preferences: PUT /api/preferences
        UpdateJobPreferencesRequest updateReq = UpdateJobPreferencesRequest.builder()
                .targetRoles(List.of("Staff Java Engineer", "Spring Boot Architect"))
                .roleVariations(List.of("Principal Engineer", "Cloud Backend Lead"))
                .locations(List.of("Bengaluru, India", "Remote"))
                .workModes(List.of("HYBRID", "REMOTE"))
                .minSalary(BigDecimal.valueOf(1200000.00))
                .maxSalary(BigDecimal.valueOf(2400000.00))
                .minExperience(BigDecimal.valueOf(2.0))
                .maxExperience(BigDecimal.valueOf(6.0))
                .industries(List.of("Fintech", "Enterprise Cloud"))
                .requiredSkills(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL"))
                .preferredSkills(List.of("Docker", "Kubernetes", "AWS"))
                .excludedCompanies(List.of("TechNova Technologies")) // Exclude TechNova
                .excludedKeywords(List.of("Unpaid", "PHP"))
                .jobTypes(List.of("FULL_TIME"))
                .autoApplyEnabled(true)
                .autoApplyMinScore(88)
                .autoApplyDailyLimit(8)
                .requireApproval(true)
                .build();

        mockMvc.perform(put("/api/preferences")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoApplyMinScore").value(88))
                .andExpect(jsonPath("$.data.autoApplyDailyLimit").value(8))
                .andExpect(jsonPath("$.data.excludedCompanies[0]").value("TechNova Technologies"));

        // 3. Verify Job Discovery Engine respects excluded companies filter
        List<JobDto> discovered = jobDiscoveryService.runDiscoveryPipeline(userEmail, JobSearchCriteria.builder().limit(20).build());

        boolean containsExcludedCompany = discovered.stream()
                .anyMatch(j -> j.getCompany().equalsIgnoreCase("TechNova Technologies"));

        assertFalse(containsExcludedCompany, "Job from excluded company TechNova Technologies must be filtered out by Discovery Agent");
    }

    @Test
    void testInvalidThresholdValidation() throws Exception {
        UpdateJobPreferencesRequest invalidReq = UpdateJobPreferencesRequest.builder()
                .autoApplyMinScore(30) // Minimum allowed is 50
                .autoApplyDailyLimit(0) // Minimum allowed is 1
                .build();

        mockMvc.perform(put("/api/preferences")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
