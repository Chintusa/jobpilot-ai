package com.jobpilot.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String currentTitle;
    private String summary;
    private BigDecimal totalExperienceYears;
    private BigDecimal targetSalaryMin;
    private BigDecimal targetSalaryMax;
    private String location;
    private String preferredWorkMode;
    private Boolean isApproved;
    private List<CandidateProfileDto.SkillDto> skills;
    private List<CandidateProfileDto.ExperienceDto> experiences;
    private List<CandidateProfileDto.EducationDto> educations;
}
