package com.jobpilot.matching.dto;

import com.jobpilot.matching.entity.JobMatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchDto {

    private UUID id;
    private UUID jobId;
    private Integer overallScore;
    private String classification;
    private String recommendation;
    private String scoreBreakdown;
    private String reasoning;
    private String status;

    public static JobMatchDto fromEntity(JobMatch m) {
        if (m == null) return null;
        return JobMatchDto.builder()
                .id(m.getId())
                .jobId(m.getJob().getId())
                .overallScore(m.getOverallScore())
                .classification(m.getClassification())
                .recommendation(m.getRecommendation())
                .scoreBreakdown(m.getScoreBreakdown())
                .reasoning(m.getReasoning())
                .status(m.getStatus())
                .build();
    }
}
