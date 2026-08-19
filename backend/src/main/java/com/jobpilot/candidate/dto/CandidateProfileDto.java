package com.jobpilot.candidate.dto;

import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileEducation;
import com.jobpilot.candidate.entity.ProfileExperience;
import com.jobpilot.candidate.entity.ProfileSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileDto {

    private UUID id;
    private String currentTitle;
    private String summary;
    private BigDecimal totalExperienceYears;
    private BigDecimal targetSalaryMin;
    private BigDecimal targetSalaryMax;
    private String location;
    private String preferredWorkMode;
    private Integer completenessScore;
    private boolean isApproved;
    private String projectsJson;
    private String certificationsJson;
    private String achievementsJson;
    private List<SkillDto> skills;
    private List<ExperienceDto> experiences;
    private List<EducationDto> educations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDto {
        private UUID id;
        private String name;
        private String category;
        private String proficiency;
        private String evidenceType;
        private BigDecimal yearsExperience;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceDto {
        private UUID id;
        private String company;
        private String title;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean current;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationDto {
        private UUID id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private String grade;
    }

    public static CandidateProfileDto fromEntity(CandidateProfile profile) {
        if (profile == null) return null;
        return CandidateProfileDto.builder()
                .id(profile.getId())
                .currentTitle(profile.getCurrentTitle())
                .summary(profile.getSummary())
                .totalExperienceYears(profile.getTotalExperienceYears())
                .targetSalaryMin(profile.getTargetSalaryMin())
                .targetSalaryMax(profile.getTargetSalaryMax())
                .location(profile.getLocation())
                .preferredWorkMode(profile.getPreferredWorkMode())
                .completenessScore(profile.getCompletenessScore())
                .isApproved(profile.isApproved())
                .projectsJson(profile.getProjectsJson())
                .certificationsJson(profile.getCertificationsJson())
                .achievementsJson(profile.getAchievementsJson())
                .skills(profile.getSkills() != null ? profile.getSkills().stream().map(s -> SkillDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .category(s.getCategory())
                        .proficiency(s.getProficiency())
                        .evidenceType(s.getEvidenceType())
                        .yearsExperience(s.getYearsExperience())
                        .build()).collect(Collectors.toList()) : List.of())
                .experiences(profile.getExperiences() != null ? profile.getExperiences().stream().map(e -> ExperienceDto.builder()
                        .id(e.getId())
                        .company(e.getCompany())
                        .title(e.getTitle())
                        .location(e.getLocation())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .current(e.isCurrent())
                        .description(e.getDescription())
                        .build()).collect(Collectors.toList()) : List.of())
                .educations(profile.getEducations() != null ? profile.getEducations().stream().map(ed -> EducationDto.builder()
                        .id(ed.getId())
                        .institution(ed.getInstitution())
                        .degree(ed.getDegree())
                        .fieldOfStudy(ed.getFieldOfStudy())
                        .startYear(ed.getStartYear())
                        .endYear(ed.getEndYear())
                        .grade(ed.getGrade())
                        .build()).collect(Collectors.toList()) : List.of())
                .build();
    }
}
