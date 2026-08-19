package com.jobpilot.ai.dto;

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
public class ExtractedCandidateProfileJson {

    private String currentTitle;
    private String summary;
    private BigDecimal totalExperienceYears;
    private List<String> locations;
    private List<ExtractedSkill> skills;
    private List<ExtractedExperience> experiences;
    private List<ExtractedEducation> educations;
    private List<ExtractedProject> projects;
    private List<String> certifications;
    private List<String> technologies;
    private List<String> achievements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedSkill {
        private String name;
        private String category;
        private String proficiency; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
        private String evidenceType; // DEMONSTRATED, MENTIONED, INFERRED, WEAK, UNKNOWN
        private BigDecimal yearsExperience;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedExperience {
        private String company;
        private String title;
        private String location;
        private String startDate; // YYYY-MM-DD
        private String endDate;   // YYYY-MM-DD
        private boolean current;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedEducation {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private Integer startYear;
        private Integer endYear;
        private String grade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedProject {
        private String name;
        private String description;
        private List<String> technologies;
        private String url;
    }
}
