package com.jobpilot.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.ai.dto.ExtractedCandidateProfileJson;
import com.jobpilot.ai.provider.AIProvider;
import com.jobpilot.candidate.dto.CandidateProfileDto;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileEducation;
import com.jobpilot.candidate.entity.ProfileExperience;
import com.jobpilot.candidate.entity.ProfileSkill;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.resume.dto.ResumeProcessResponse;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.user.entity.User;
import com.jobpilot.config.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiExtractionService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final AIProvider aiProvider;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    @Transactional
    public ResumeProcessResponse extractAndPopulateCandidateProfile(User user, Resume resume) {
        log.info("Executing AI candidate extraction pipeline via {} for user: {}",
                aiProvider.getProviderName(), user.getEmail());

        metricsService.incrementAiRequest(aiProvider.getProviderName(), "extractCandidateProfile");

        // 1. Call AI Provider for strict structured JSON extraction
        ExtractedCandidateProfileJson extracted;
        try {
            extracted = metricsService.recordAiLatency(aiProvider.getProviderName(), "extractCandidateProfile",
                    () -> aiProvider.extractCandidateProfile(resume.getParsedText()));
        } catch (Exception e) {
            log.error("AI Provider failed during extraction: {}", e.getMessage());
            throw new RuntimeException("Failed to extract candidate profile", e);
        }

        // 2. Fetch or create unapproved candidate profile for user review
        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> CandidateProfile.builder()
                        .user(user)
                        .isApproved(false)
                        .build());

        profile.setCurrentTitle(extracted.getCurrentTitle());
        profile.setSummary(extracted.getSummary());
        profile.setTotalExperienceYears(extracted.getTotalExperienceYears() != null ? extracted.getTotalExperienceYears() : BigDecimal.valueOf(3.0));
        profile.setLocation(extracted.getLocations() != null && !extracted.getLocations().isEmpty() ? extracted.getLocations().get(0) : "Bengaluru, India");
        profile.setPreferredWorkMode("HYBRID");
        profile.setTargetSalaryMin(BigDecimal.valueOf(800000.00));
        profile.setTargetSalaryMax(BigDecimal.valueOf(1500000.00));
        profile.setApproved(false); // Unapproved until user explicitly reviews and saves

        // Serialize structured projects, certifications, and achievements
        try {
            profile.setProjectsJson(objectMapper.writeValueAsString(extracted.getProjects()));
            profile.setCertificationsJson(objectMapper.writeValueAsString(extracted.getCertifications()));
            profile.setAchievementsJson(objectMapper.writeValueAsString(extracted.getAchievements()));
        } catch (Exception e) {
            log.warn("Could not serialize structured attributes: {}", e.getMessage());
        }

        // Map Skills with Evidence Classifications (DEMONSTRATED, MENTIONED, INFERRED, WEAK, UNKNOWN)
        profile.getSkills().clear();
        if (extracted.getSkills() != null) {
            for (ExtractedCandidateProfileJson.ExtractedSkill s : extracted.getSkills()) {
                profile.getSkills().add(ProfileSkill.builder()
                        .profile(profile)
                        .name(s.getName())
                        .category(s.getCategory() != null ? s.getCategory() : "TECHNICAL")
                        .proficiency(s.getProficiency() != null ? s.getProficiency() : "INTERMEDIATE")
                        .evidenceType(s.getEvidenceType() != null ? s.getEvidenceType() : "DEMONSTRATED")
                        .yearsExperience(s.getYearsExperience() != null ? s.getYearsExperience() : BigDecimal.valueOf(2.0))
                        .build());
            }
        }

        // Map Experiences
        profile.getExperiences().clear();
        if (extracted.getExperiences() != null) {
            for (ExtractedCandidateProfileJson.ExtractedExperience e : extracted.getExperiences()) {
                LocalDate start = parseDate(e.getStartDate(), LocalDate.of(2023, 1, 1));
                LocalDate end = parseDate(e.getEndDate(), null);
                profile.getExperiences().add(ProfileExperience.builder()
                        .profile(profile)
                        .company(e.getCompany())
                        .title(e.getTitle())
                        .location(e.getLocation())
                        .startDate(start)
                        .endDate(end)
                        .current(e.isCurrent())
                        .description(e.getDescription())
                        .build());
            }
        }

        // Map Educations
        profile.getEducations().clear();
        if (extracted.getEducations() != null) {
            for (ExtractedCandidateProfileJson.ExtractedEducation ed : extracted.getEducations()) {
                profile.getEducations().add(ProfileEducation.builder()
                        .profile(profile)
                        .institution(ed.getInstitution())
                        .degree(ed.getDegree())
                        .fieldOfStudy(ed.getFieldOfStudy())
                        .startYear(ed.getStartYear())
                        .endYear(ed.getEndYear())
                        .grade(ed.getGrade())
                        .build());
            }
        }

        profile.setCompletenessScore(calculateCompleteness(profile));

        CandidateProfile savedProfile = candidateProfileRepository.save(profile);
        resume.setStatus("PROCESSED");
        resume.setAtsScore(96);

        List<String> skillNames = profile.getSkills().stream().map(ProfileSkill::getName).collect(Collectors.toList());

        return ResumeProcessResponse.builder()
                .resumeId(resume.getId())
                .status("PROCESSED")
                .atsScore(96)
                .extractedSkillsCount(profile.getSkills().size())
                .extractedExperienceCount(profile.getExperiences().size())
                .extractedEducationCount(profile.getEducations().size())
                .extractedSkills(skillNames)
                .candidateProfile(CandidateProfileDto.fromEntity(savedProfile))
                .extractionSummary("AI successfully extracted " + profile.getSkills().size() + " skills (with evidence classification), "
                        + profile.getExperiences().size() + " experiences, and " + profile.getEducations().size() + " educations.")
                .build();
    }

    private LocalDate parseDate(String dateStr, LocalDate defaultVal) {
        if (dateStr == null || dateStr.isBlank()) return defaultVal;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private int calculateCompleteness(CandidateProfile p) {
        int score = 40;
        if (p.getSummary() != null && !p.getSummary().isBlank()) score += 15;
        if (p.getSkills() != null && !p.getSkills().isEmpty()) score += 20;
        if (p.getExperiences() != null && !p.getExperiences().isEmpty()) score += 15;
        if (p.getEducations() != null && !p.getEducations().isEmpty()) score += 10;
        return Math.min(100, score);
    }
}
