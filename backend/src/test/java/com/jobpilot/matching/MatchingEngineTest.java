package com.jobpilot.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileEducation;
import com.jobpilot.candidate.entity.ProfileSkill;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.service.MatchingEngineService;
import com.jobpilot.preferences.entity.JobPreferences;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchingEngineTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchingEngineService matchingEngineService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobSourceRepository jobSourceRepository;

    private String authToken;
    private String userEmail;
    private Job sampleJob;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "matching.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Matching Tester")
                .phone("+91 9988776655")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        authToken = objectMapper.readTree(authResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        JobSource source = jobSourceRepository.findByName("MATCH_TEST_SOURCE")
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name("MATCH_TEST_SOURCE")
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .lastSyncAt(Instant.now())
                        .build()));

        sampleJob = jobRepository.findBySourceIdAndExternalId(source.getId(), "MATCH-JOB-001")
                .orElseGet(() -> jobRepository.save(Job.builder()
                        .source(source)
                        .externalId("MATCH-JOB-001")
                        .title("Senior Java Microservices Engineer")
                        .company("FinTech Innovations")
                        .location("Bengaluru, India")
                        .workMode("HYBRID")
                        .salaryMin(BigDecimal.valueOf(1600000))
                        .salaryMax(BigDecimal.valueOf(2400000))
                        .experienceMin(BigDecimal.valueOf(3.0))
                        .experienceMax(BigDecimal.valueOf(6.0))
                        .description("Designing scalable microservices using Java 21 and Spring Boot.")
                        .requiredSkills("[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"Microservices\"]")
                        .preferredSkills("[\"Docker\", \"AWS\", \"Redis\"]")
                        .canonicalUrl("https://example.com/jobs/match-001")
                        .dedupHash("fintechinnovations|seniorjavamicroservicesengineer|bengaluruindia")
                        .status("ACTIVE")
                        .build()));
    }

    @Test
    void testExactRecruiterWeightsAndClassification() {
        CandidateProfile profile = CandidateProfile.builder()
                .currentTitle("Senior Java Engineer")
                .totalExperienceYears(BigDecimal.valueOf(4.0))
                .location("Bengaluru, India")
                .isApproved(true)
                .skills(List.of(
                        ProfileSkill.builder().name("Java").evidenceType("DEMONSTRATED").build(),
                        ProfileSkill.builder().name("Spring Boot").evidenceType("DEMONSTRATED").build(),
                        ProfileSkill.builder().name("PostgreSQL").evidenceType("DEMONSTRATED").build(),
                        ProfileSkill.builder().name("Microservices").evidenceType("DEMONSTRATED").build()
                ))
                .educations(List.of(
                        ProfileEducation.builder().institution("NIT").degree("B.Tech").fieldOfStudy("CSE").build()
                ))
                .build();

        JobPreferences preferences = JobPreferences.builder()
                .autoApplyMinScore(85)
                .build();

        MatchingEngineService.RecruiterScoreMatrix matrix = matchingEngineService.evaluateJobMatch(profile, preferences, sampleJob);

        // Verify exact category weights bounds
        assertTrue(matrix.getEligibilityScore() <= 25 && matrix.getEligibilityScore() >= 0);
        assertTrue(matrix.getTechnicalSkillsScore() <= 25 && matrix.getTechnicalSkillsScore() >= 0);
        assertTrue(matrix.getRelevantExperienceScore() <= 15 && matrix.getRelevantExperienceScore() >= 0);
        assertTrue(matrix.getRoleSeniorityScore() <= 10 && matrix.getRoleSeniorityScore() >= 0);
        assertTrue(matrix.getEducationScore() <= 5 && matrix.getEducationScore() >= 0);
        assertTrue(matrix.getLocationWorkModeScore() <= 5 && matrix.getLocationWorkModeScore() >= 0);
        assertTrue(matrix.getProjectRelevanceScore() <= 5 && matrix.getProjectRelevanceScore() >= 0);
        assertTrue(matrix.getOverallRecruiterAppeal() <= 10 && matrix.getOverallRecruiterAppeal() >= 0);

        // Total score calculation
        assertEquals(matrix.getTotalScore(),
                matrix.getEligibilityScore() + matrix.getTechnicalSkillsScore() +
                matrix.getRelevantExperienceScore() + matrix.getRoleSeniorityScore() +
                matrix.getEducationScore() + matrix.getLocationWorkModeScore() +
                matrix.getProjectRelevanceScore() + matrix.getOverallRecruiterAppeal());

        assertTrue(matrix.getTotalScore() >= 90, "Strong candidate profile must score in EXCELLENT tier");
        assertEquals("EXCELLENT", matrix.getClassification());
        assertEquals("APPLY", matrix.getRecommendation());
        assertNotNull(matrix.getAuditReasoning());
    }

    @Test
    void testExclusionPolicyResultsInSkipRecommendation() {
        CandidateProfile profile = CandidateProfile.builder()
                .currentTitle("Java Developer")
                .totalExperienceYears(BigDecimal.valueOf(2.0))
                .location("Bengaluru, India")
                .build();

        JobPreferences excludedPrefs = JobPreferences.builder()
                .excludedCompanies("[\"FinTech Innovations\"]") // Excluded company
                .autoApplyMinScore(80)
                .build();

        MatchingEngineService.RecruiterScoreMatrix matrix = matchingEngineService.evaluateJobMatch(profile, excludedPrefs, sampleJob);

        assertEquals(0, matrix.getEligibilityScore(), "Excluded company must result in 0 eligibility score");
        assertTrue(matrix.getTotalScore() < 80);
        assertTrue(matrix.getAuditReasoning().contains("Excluded by candidate employer/keyword policy"));
    }

    @Test
    void testGetJobMatchEndpoint() throws Exception {
        mockMvc.perform(get("/api/jobs/" + sampleJob.getId() + "/match")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value(sampleJob.getId().toString()))
                .andExpect(jsonPath("$.data.overallScore").isNumber())
                .andExpect(jsonPath("$.data.classification").isString())
                .andExpect(jsonPath("$.data.recommendation").isString())
                .andExpect(jsonPath("$.data.scoreBreakdown").isNotEmpty())
                .andExpect(jsonPath("$.data.reasoning").isNotEmpty());
    }
}
