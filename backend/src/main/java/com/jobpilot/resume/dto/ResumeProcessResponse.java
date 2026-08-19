package com.jobpilot.resume.dto;

import com.jobpilot.candidate.dto.CandidateProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeProcessResponse {

    private UUID resumeId;
    private String status;
    private Integer atsScore;
    private int extractedSkillsCount;
    private int extractedExperienceCount;
    private int extractedEducationCount;
    private List<String> extractedSkills;
    private CandidateProfileDto candidateProfile;
    private String extractionSummary;
}
