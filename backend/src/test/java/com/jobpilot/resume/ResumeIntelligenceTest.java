package com.jobpilot.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.common.exception.BadRequestException;
import com.jobpilot.resume.service.ResumeParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeIntelligenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResumeParserService resumeParserService;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        String uniqueEmail = "resume.tester." + System.currentTimeMillis() + "@example.com";
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(uniqueEmail)
                .password("Password123!")
                .name("Resume Intelligence Tester")
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
    void testValidPdfParsingWithPdfBox() throws Exception {
        // Create valid PDF document with Apache PDFBox
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                content.newLineAtOffset(50, 700);
                content.showText("Senior Java Backend Engineer with Spring Boot and AWS expertise.");
                content.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile validPdf = new MockMultipartFile(
                "file",
                "Senior_Java_Engineer.pdf",
                "application/pdf",
                pdfBytes
        );

        // Direct parser check
        String parsed = resumeParserService.parseResume(validPdf);
        assertNotNull(parsed);
        assertTrue(parsed.contains("Senior Java Backend Engineer"));

        // Upload API check
        mockMvc.perform(multipart("/api/resumes")
                        .file(validPdf)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PARSED"))
                .andExpect(jsonPath("$.data.fileName").value("Senior_Java_Engineer.pdf"));
    }

    @Test
    void testValidDocxParsingWithApachePoi() throws Exception {
        // Create valid DOCX document with Apache POI
        byte[] docxBytes;
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText("Java Developer experienced in Spring Boot, PostgreSQL, and Redis caching.");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.write(baos);
            docxBytes = baos.toByteArray();
        }

        MockMultipartFile validDocx = new MockMultipartFile(
                "file",
                "Backend_Resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes
        );

        // Direct parser check
        String parsed = resumeParserService.parseResume(validDocx);
        assertNotNull(parsed);
        assertTrue(parsed.contains("Java Developer experienced in Spring Boot"));

        // Upload API check
        mockMvc.perform(multipart("/api/resumes")
                        .file(validDocx)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PARSED"));
    }

    @Test
    void testInvalidFileTypeRejection() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "MZ executable content".getBytes()
        );

        mockMvc.perform(multipart("/api/resumes")
                        .file(exeFile)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMalformedPdfRejection() {
        // PDF extension but invalid magic bytes
        MockMultipartFile fakePdf = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                "This is not a real PDF file header".getBytes()
        );

        assertThrows(BadRequestException.class, () -> resumeParserService.parseResume(fakePdf));
    }
}
