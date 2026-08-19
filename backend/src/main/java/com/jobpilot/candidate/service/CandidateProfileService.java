package com.jobpilot.candidate.service;

import com.jobpilot.candidate.dto.CandidateProfileDto;
import com.jobpilot.candidate.dto.UpdateProfileRequest;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileEducation;
import com.jobpilot.candidate.entity.ProfileExperience;
import com.jobpilot.candidate.entity.ProfileSkill;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CandidateProfileDto getProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        CandidateProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultProfile(user));

        return CandidateProfileDto.fromEntity(profile);
    }

    @Transactional
    public CandidateProfileDto updateProfile(String userEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        CandidateProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultProfile(user));

        if (request.getCurrentTitle() != null) {
            profile.setCurrentTitle(request.getCurrentTitle());
        }
        if (request.getSummary() != null) {
            profile.setSummary(request.getSummary());
        }
        if (request.getTotalExperienceYears() != null) {
            profile.setTotalExperienceYears(request.getTotalExperienceYears());
        }
        if (request.getTargetSalaryMin() != null) {
            profile.setTargetSalaryMin(request.getTargetSalaryMin());
        }
        if (request.getTargetSalaryMax() != null) {
            profile.setTargetSalaryMax(request.getTargetSalaryMax());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getPreferredWorkMode() != null) {
            profile.setPreferredWorkMode(request.getPreferredWorkMode());
        }

        // Update skills if provided
        if (request.getSkills() != null) {
            profile.getSkills().clear();
            for (CandidateProfileDto.SkillDto s : request.getSkills()) {
                profile.getSkills().add(ProfileSkill.builder()
                        .profile(profile)
                        .name(s.getName())
                        .category(s.getCategory() != null ? s.getCategory() : "TECHNICAL")
                        .proficiency(s.getProficiency() != null ? s.getProficiency() : "ADVANCED")
                        .evidenceType(s.getEvidenceType() != null ? s.getEvidenceType() : "DEMONSTRATED")
                        .yearsExperience(s.getYearsExperience() != null ? s.getYearsExperience() : BigDecimal.valueOf(2.0))
                        .build());
            }
        }

        // Update experiences if provided
        if (request.getExperiences() != null) {
            profile.getExperiences().clear();
            for (CandidateProfileDto.ExperienceDto e : request.getExperiences()) {
                profile.getExperiences().add(ProfileExperience.builder()
                        .profile(profile)
                        .company(e.getCompany())
                        .title(e.getTitle())
                        .location(e.getLocation())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .current(e.isCurrent())
                        .description(e.getDescription())
                        .build());
            }
        }

        // Update educations if provided
        if (request.getEducations() != null) {
            profile.getEducations().clear();
            for (CandidateProfileDto.EducationDto ed : request.getEducations()) {
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

        // Mark as user approved and verified
        profile.setApproved(true);

        // Recalculate completeness score
        int score = calculateCompleteness(profile);
        profile.setCompletenessScore(score);

        CandidateProfile saved = profileRepository.save(profile);
        log.info("Candidate profile for {} approved and saved successfully (Completeness: {}%)", userEmail, score);
        return CandidateProfileDto.fromEntity(saved);
    }

    @Transactional
    public CandidateProfileDto addSkill(String userEmail, String skillName, String category) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CandidateProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultProfile(user));

        ProfileSkill skill = ProfileSkill.builder()
                .profile(profile)
                .name(skillName)
                .category(category != null ? category : "TECHNICAL")
                .proficiency("ADVANCED")
                .evidenceType("DEMONSTRATED")
                .build();

        profile.getSkills().add(skill);
        profile.setCompletenessScore(calculateCompleteness(profile));
        CandidateProfile saved = profileRepository.save(profile);
        return CandidateProfileDto.fromEntity(saved);
    }

    private int calculateCompleteness(CandidateProfile p) {
        int score = 40; // baseline
        if (p.getSummary() != null && !p.getSummary().isBlank()) score += 15;
        if (p.getSkills() != null && !p.getSkills().isEmpty()) score += 20;
        if (p.getExperiences() != null && !p.getExperiences().isEmpty()) score += 15;
        if (p.getEducations() != null && !p.getEducations().isEmpty()) score += 10;
        return Math.min(100, score);
    }

    private CandidateProfile createDefaultProfile(User user) {
        CandidateProfile profile = CandidateProfile.builder()
                .user(user)
                .currentTitle("Java Backend Developer")
                .summary("Enthusiastic Java developer with hands-on experience in Spring Boot and microservices architecture.")
                .totalExperienceYears(BigDecimal.valueOf(2.5))
                .targetSalaryMin(BigDecimal.valueOf(600000.00))
                .targetSalaryMax(BigDecimal.valueOf(1200000.00))
                .location("Bengaluru, India")
                .preferredWorkMode("HYBRID")
                .completenessScore(94)
                .build();
        return profileRepository.save(profile);
    }
}
