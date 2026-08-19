package com.jobpilot.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.jobs.service.JobDiscoveryService;
import com.jobpilot.jobs.source.ExternalJob;
import com.jobpilot.jobs.source.JobNormalizer;
import com.jobpilot.jobs.source.JobSearchCriteria;
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
class JobDiscoveryModularTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobNormalizer jobNormalizer;

    @Autowired
    private JobDiscoveryService jobDiscoveryService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    private String authToken;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "discovery.test." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Discovery Tester")
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
    void testCanonicalUrlCleansingAndDedupHash() {
        String dirtyUrl = "https://JOBS.EXAMPLE.COM/careers/software-engineer/?utm_source=linkedin&utm_medium=job_post&ref=referral_code_123&fbclid=abcdef#apply";
        String cleanUrl = jobNormalizer.normalizeUrl(dirtyUrl);

        assertEquals("https://jobs.example.com/careers/software-engineer", cleanUrl);

        String hash1 = jobNormalizer.computeDedupHash("Acme Corp", "Senior Java Engineer", "Bengaluru, India");
        String hash2 = jobNormalizer.computeDedupHash("acme corp ", " senior java engineer ", "bengaluru, india");
        assertEquals(hash1, hash2, "Dedup hashes must be deterministic and invariant under casing/whitespace");
    }

    @Test
    void testJobNormalization() {
        JobSource source = jobSourceRepository.save(JobSource.builder()
                .name("TEST_NORMALIZER_SOURCE")
                .enabled(true)
                .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                .lastSyncAt(Instant.now())
                .build());

        ExternalJob externalJob = ExternalJob.builder()
                .externalId("TEST-EXT-001")
                .sourceName("TEST_NORMALIZER_SOURCE")
                .rawTitle("  Senior Full Stack Java Engineer  ")
                .rawCompany("  CloudNova Inc. ")
                .rawLocation("bangalore ")
                .rawWorkMode("hybrid-remote")
                .salaryMin(BigDecimal.valueOf(1400000))
                .salaryMax(BigDecimal.valueOf(2200000))
                .salaryCurrency("INR")
                .salaryDisplay("₹14.0 - 22.0 LPA")
                .experienceMin(BigDecimal.valueOf(3.0))
                .experienceMax(BigDecimal.valueOf(7.0))
                .rawDescription("Building enterprise cloud microservices.")
                .rawSkills(List.of("Java", "Spring Boot", "AWS"))
                .jobUrl("https://example.com/jobs/001?utm_source=google")
                .build();

        Job normalized = jobNormalizer.normalize(externalJob, source);

        assertNotNull(normalized);
        assertEquals("Senior Full Stack Java Engineer", normalized.getTitle());
        assertEquals("CloudNova Inc.", normalized.getCompany());
        assertEquals("Bengaluru, India", normalized.getLocation());
        assertEquals("REMOTE", normalized.getWorkMode());
        assertEquals("https://example.com/jobs/001", normalized.getCanonicalUrl());
        assertNotNull(normalized.getDedupHash());
    }

    @Test
    void testDeduplicationAndDiscoverySearchDetailApis() throws Exception {
        // 1. Trigger Discovery Pipeline: POST /api/jobs/sync
        MvcResult syncResult = mockMvc.perform(post("/api/jobs/sync")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(JobSearchCriteria.builder().limit(20).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        long countAfterFirstSync = jobRepository.count();
        assertTrue(countAfterFirstSync > 0, "Jobs must be persisted in database");

        // 2. Trigger second sync with same source - verify no duplicate inserts occur
        mockMvc.perform(post("/api/jobs/sync")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(JobSearchCriteria.builder().limit(20).build())))
                .andExpect(status().isOk());

        long countAfterSecondSync = jobRepository.count();
        assertEquals(countAfterFirstSync, countAfterSecondSync, "Deduplication must prevent duplicate job records on subsequent syncs");

        // 3. Test Search API: GET /api/jobs?keyword=Java
        MvcResult searchResult = mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Java")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();

        String searchJson = searchResult.getResponse().getContentAsString();
        String firstJobId = objectMapper.readTree(searchJson)
                .path("data").path("content").get(0).path("id").asText();

        // 4. Test Job Detail API: GET /api/jobs/{id}
        mockMvc.perform(get("/api/jobs/" + firstJobId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(firstJobId))
                .andExpect(jsonPath("$.data.canonicalUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.sourceName").isNotEmpty());
    }
}
