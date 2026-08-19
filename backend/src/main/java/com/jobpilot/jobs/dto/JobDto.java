package com.jobpilot.jobs.dto;

import com.jobpilot.jobs.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {

    private UUID id;
    private String title;
    private String company;
    private String location;
    private String workMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String salaryDisplay;
    private BigDecimal experienceMin;
    private BigDecimal experienceMax;
    private String description;
    private String requiredSkills;
    private String preferredSkills;
    private String jobUrl;
    private String canonicalUrl;
    private String sourceName;
    private String status;
    private Instant postedAt;
    private Integer matchScore;

    public static JobDto fromEntity(Job job) {
        if (job == null) return null;
        return JobDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .salaryDisplay(job.getSalaryDisplay())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .description(job.getDescription())
                .requiredSkills(job.getRequiredSkills())
                .preferredSkills(job.getPreferredSkills())
                .jobUrl(job.getJobUrl())
                .canonicalUrl(job.getCanonicalUrl())
                .sourceName(job.getSource() != null ? job.getSource().getName() : "MODULAR_SOURCE")
                .status(job.getStatus())
                .postedAt(job.getPostedAt())
                .build();
    }
}
