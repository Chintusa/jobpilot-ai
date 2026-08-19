package com.jobpilot.resume.service;

import com.jobpilot.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@Service
public class ResumeParserService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final byte[] PDF_MAGIC_BYTES = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] ZIP_MAGIC_BYTES = new byte[]{0x50, 0x4B}; // PK (DOCX)

    /**
     * Validates file security and extracts text via Apache PDFBox (for PDF) or Apache POI (for DOCX).
     * Note: Resume text contents are NEVER logged for candidate privacy.
     */
    public String parseResume(MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String lowerName = (originalFilename != null ? originalFilename : "").toLowerCase();

        long startTime = System.currentTimeMillis();
        String extractedText;

        if (lowerName.endsWith(".pdf")) {
            extractedText = parsePdfWithPdfBox(file);
        } else if (lowerName.endsWith(".docx")) {
            extractedText = parseDocxWithPoi(file);
        } else {
            throw new BadRequestException("Unsupported file type. Only PDF and DOCX documents are accepted.");
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully parsed resume document: name='{}', size={} bytes, extracted_chars={}, duration={}ms",
                originalFilename, file.getSize(), extractedText.length(), duration);

        return extractedText;
    }

    /**
     * File validation covering type, extension, size, and header magic bytes.
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded resume file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Resume file exceeds the maximum allowed size of 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Resume file must have a valid filename");
        }

        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".docx")) {
            throw new BadRequestException("Invalid file extension. Only .pdf and .docx files are permitted");
        }

        // Magic bytes validation
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 2) {
                throw new BadRequestException("Malformed or corrupted resume file");
            }

            if (lowerName.endsWith(".pdf")) {
                if (!startsWith(header, PDF_MAGIC_BYTES)) {
                    throw new BadRequestException("File header is invalid for PDF format. The file may be corrupt or misnamed.");
                }
            } else if (lowerName.endsWith(".docx")) {
                if (!startsWith(header, ZIP_MAGIC_BYTES)) {
                    throw new BadRequestException("File header is invalid for DOCX format. The file may be corrupt or misnamed.");
                }
            }
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
            log.error("Failed to inspect file magic bytes: {}", e.getMessage());
            throw new BadRequestException("Unable to read or inspect uploaded file");
        }
    }

    private String parsePdfWithPdfBox(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                if (document.isEncrypted()) {
                    throw new BadRequestException("Password-protected or encrypted PDF files are not supported");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                if (text == null || text.trim().isEmpty()) {
                    throw new BadRequestException("PDF document contains no extractable text (it may be scanned or empty)");
                }
                return text.trim();
            }
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
            log.warn("Apache PDFBox failed to parse PDF document: {}", e.getMessage());
            throw new BadRequestException("Malformed or corrupted PDF document");
        }
    }

    private String parseDocxWithPoi(MultipartFile file) {
        try (InputStream is = new ByteArrayInputStream(file.getBytes());
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            if (text == null || text.trim().isEmpty()) {
                throw new BadRequestException("DOCX document contains no extractable text");
            }
            return text.trim();
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception e) {
            log.warn("Apache POI failed to parse DOCX document: {}", e.getMessage());
            throw new BadRequestException("Malformed or corrupted DOCX document");
        }
    }

    private boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) return false;
        }
        return true;
    }
}
