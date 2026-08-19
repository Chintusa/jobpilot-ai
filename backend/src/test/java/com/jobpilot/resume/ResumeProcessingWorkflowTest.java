package com.jobpilot.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.candidate.dto.CandidateProfileDto;
import com.jobpilot.candidate.dto.UpdateProfileRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeProcessingWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEndToEndResumeToProfileWorkflow() throws Exception {
        // 1. Register candidate user
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("alex.candidate@example.com")
                .password("Password123")
                .name("Alex Candidate")
                .phone("+91 9123456789")
                .build();

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = authResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).path("data").path("accessToken").asText();

        // 2. Generate valid PDF bytes with Apache PDFBox
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Senior Java Engineer with 5 years experience in Spring Boot, REST APIs, Microservices, SQL, Docker, and AWS.");
                content.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile resumeFile = new MockMultipartFile(
                "file",
                "Alex_Candidate_Resume.pdf",
                "application/pdf",
                pdfBytes
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/resumes")
                        .file(resumeFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("Alex_Candidate_Resume.pdf"))
                .andExpect(jsonPath("$.data.status").value("PARSED"))
                .andReturn();

        String uploadJson = uploadResult.getResponse().getContentAsString();
        String resumeId = objectMapper.readTree(uploadJson).path("data").path("id").asText();

        // 3. Process Resume: POST /api/resumes/{id}/process
        mockMvc.perform(post("/api/resumes/" + resumeId + "/process")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PROCESSED"))
                .andExpect(jsonPath("$.data.extractedSkillsCount").isNumber())
                .andExpect(jsonPath("$.data.candidateProfile.currentTitle").isNotEmpty());

        // 4. Fetch Candidate Profile: GET /api/candidate-profile
        mockMvc.perform(get("/api/candidate-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentTitle").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.data.completenessScore").isNumber());

        // 5. User edits and approves profile: PUT /api/candidate-profile
        UpdateProfileRequest updateReq = UpdateProfileRequest.builder()
                .currentTitle("Lead Java Backend Developer")
                .summary("Updated by user: 5+ years building enterprise Spring Boot systems.")
                .location("Bengaluru, Karnataka, India")
                .preferredWorkMode("HYBRID")
                .targetSalaryMin(BigDecimal.valueOf(1200000.00))
                .targetSalaryMax(BigDecimal.valueOf(1800000.00))
                .isApproved(true)
                .skills(List.of(
                        CandidateProfileDto.SkillDto.builder().name("Java").category("BACKEND").proficiency("ADVANCED").build(),
                        CandidateProfileDto.SkillDto.builder().name("Spring Boot").category("BACKEND").proficiency("ADVANCED").build(),
                        CandidateProfileDto.SkillDto.builder().name("Kubernetes").category("DEVOPS").proficiency("INTERMEDIATE").build()
                ))
                .build();

        mockMvc.perform(put("/api/candidate-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentTitle").value("Lead Java Backend Developer"))
                .andExpect(jsonPath("$.data.location").value("Bengaluru, Karnataka, India"));

        // 6. Verify final saved profile: GET /api/candidate-profile
        mockMvc.perform(get("/api/candidate-profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentTitle").value("Lead Java Backend Developer"))
                .andExpect(jsonPath("$.data.skills").isArray());
    }
}
