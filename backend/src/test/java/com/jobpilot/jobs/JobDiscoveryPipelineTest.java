package com.jobpilot.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.jobs.source.JobSearchCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobDiscoveryPipelineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testDiscoveryToMatchingPipeline() throws Exception {
        // 1. Register candidate user
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("discovery.engineer@example.com")
                .password("Password123")
                .name("Discovery Tester")
                .phone("+91 9887766554")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 2. Trigger Discovery Pipeline: MockJobSource -> JobNormalizer -> PostgreSQL -> Matching Engine
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .locations(List.of("Bengaluru", "Remote"))
                .limit(10)
                .build();

        MvcResult syncResult = mockMvc.perform(post("/api/jobs/sync")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data[0].company").isNotEmpty())
                .andReturn();

        String syncJson = syncResult.getResponse().getContentAsString();
        String firstJobId = objectMapper.readTree(syncJson).path("data").get(0).path("id").asText();

        // 3. Verify Job Details from PostgreSQL
        mockMvc.perform(get("/api/jobs/" + firstJobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(firstJobId));

        // 4. Verify Matching Engine generated score for discovered job
        mockMvc.perform(get("/api/v1/jobs/" + firstJobId + "/match")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.recommendation").value("APPLY"));
    }
}
