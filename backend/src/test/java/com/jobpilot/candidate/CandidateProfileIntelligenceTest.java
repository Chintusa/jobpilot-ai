package com.jobpilot.candidate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.ai.dto.ExtractedCandidateProfileJson;
import com.jobpilot.ai.provider.AIProvider;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.candidate.dto.CandidateProfileDto;
import com.jobpilot.candidate.dto.UpdateProfileRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateProfileIntelligenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AIProvider aiProvider;

    private String authToken;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "profile.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(userEmail)
                .password("Password123!")
                .name("Profile Tester")
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
    void testAiProviderStrictStructuredJsonAndEvidenceClassification() {
        String resumeText = "Senior Java Engineer with 4 years experience in Spring Boot, REST APIs, and PostgreSQL. Familiar with Docker and AWS.";

        ExtractedCandidateProfileJson extracted = aiProvider.extractCandidateProfile(resumeText);

        assertNotNull(extracted);
        assertNotNull(extracted.getCurrentTitle());
        assertNotNull(extracted.getSummary());
        assertNotNull(extracted.getSkills());
        assertFalse(extracted.getSkills().isEmpty());

        // Check Evidence Classifications
        boolean hasDemonstrated = extracted.getSkills().stream().anyMatch(s -> "DEMONSTRATED".equalsIgnoreCase(s.getEvidenceType()));
        boolean hasMentioned = extracted.getSkills().stream().anyMatch(s -> "MENTIONED".equalsIgnoreCase(s.getEvidenceType()));
        boolean hasInferred = extracted.getSkills().stream().anyMatch(s -> "INFERRED".equalsIgnoreCase(s.getEvidenceType()));
        boolean hasWeak = extracted.getSkills().stream().anyMatch(s -> "WEAK".equalsIgnoreCase(s.getEvidenceType()));
        boolean hasUnknown = extracted.getSkills().stream().anyMatch(s -> "UNKNOWN".equalsIgnoreCase(s.getEvidenceType()));

        assertTrue(hasDemonstrated, "Should classify core active skills as DEMONSTRATED");
        assertTrue(hasMentioned, "Should classify listed skills as MENTIONED");
        assertTrue(hasInferred || hasWeak || hasUnknown, "Should classify non-grounded or adjacent skills appropriately");

        // Verify Education & Experience extracted
        assertFalse(extracted.getEducations().isEmpty());
        assertFalse(extracted.getExperiences().isEmpty());
        assertNotNull(extracted.getProjects());
        assertNotNull(extracted.getCertifications());
        assertNotNull(extracted.getAchievements());
    }

    @Test
    void testResumeExtractionReviewAndApprovedProfileWorkflow() throws Exception {
        // 1. Create valid PDF resume
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Senior Java Engineer with Spring Boot, PostgreSQL, Microservices, and Redis.");
                content.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile resumeFile = new MockMultipartFile(
                "file",
                "Senior_Backend_Profile.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/resumes")
                        .file(resumeFile)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andReturn();

        String resumeId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Trigger AI Extraction Pipeline: POST /api/resumes/{id}/process
        MvcResult processResult = mockMvc.perform(post("/api/resumes/" + resumeId + "/process")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.candidateProfile.approved").value(false)) // Unapproved review state
                .andExpect(jsonPath("$.data.extractedSkillsCount").isNumber())
                .andReturn();

        // 3. User reviews extracted profile: GET /api/candidate-profile
        mockMvc.perform(get("/api/candidate-profile")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(false))
                .andExpect(jsonPath("$.data.skills").isArray());

        // 4. User edits and approves profile: PUT /api/candidate-profile
        UpdateProfileRequest approvalReq = UpdateProfileRequest.builder()
                .currentTitle("Staff Backend Architect")
                .summary("Reviewed & Approved: 6+ years designing scalable distributed systems.")
                .location("Bengaluru, India")
                .preferredWorkMode("HYBRID")
                .targetSalaryMin(BigDecimal.valueOf(1500000.00))
                .targetSalaryMax(BigDecimal.valueOf(2500000.00))
                .skills(List.of(
                        CandidateProfileDto.SkillDto.builder().name("Java").category("BACKEND").proficiency("EXPERT").evidenceType("DEMONSTRATED").build(),
                        CandidateProfileDto.SkillDto.builder().name("Spring Boot").category("BACKEND").proficiency("EXPERT").evidenceType("DEMONSTRATED").build(),
                        CandidateProfileDto.SkillDto.builder().name("Kafka").category("STREAMING").proficiency("ADVANCED").evidenceType("DEMONSTRATED").build()
                ))
                .build();

        mockMvc.perform(put("/api/candidate-profile")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentTitle").value("Staff Backend Architect"))
                .andExpect(jsonPath("$.data.approved").value(true)); // Verified & Approved

        // 5. Verify final persisted approved state: GET /api/candidate-profile
        mockMvc.perform(get("/api/candidate-profile")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approved").value(true))
                .andExpect(jsonPath("$.data.currentTitle").value("Staff Backend Architect"))
                .andExpect(jsonPath("$.data.completenessScore").value(100));
    }
}
