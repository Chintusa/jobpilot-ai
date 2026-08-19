package com.jobpilot.automation.dto;

import com.jobpilot.automation.entity.HumanIntervention;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumanInterventionDto {

    private UUID id;
    private UUID applicationId;
    private String jobTitle;
    private String company;
    private String reason;
    private String type;
    private String description;
    private String status; // PENDING, IN_PROGRESS, RESOLVED, CANCELLED
    private String requiredInput;
    private String requiredInputType;
    private String context;
    private String resolutionPayload;
    private Instant createdAt;
    private Instant resolvedAt;

    public static HumanInterventionDto fromEntity(HumanIntervention i) {
        if (i == null) return null;
        String reasonVal = i.getReason() != null ? i.getReason() : i.getType();
        return HumanInterventionDto.builder()
                .id(i.getId())
                .applicationId(i.getApplication() != null ? i.getApplication().getId() : null)
                .jobTitle(i.getApplication() != null && i.getApplication().getJob() != null ? i.getApplication().getJob().getTitle() : "Carrier Portal")
                .company(i.getApplication() != null && i.getApplication().getJob() != null ? i.getApplication().getJob().getCompany() : "Target Company")
                .reason(reasonVal)
                .type(reasonVal)
                .description(i.getDescription())
                .status(i.getStatus())
                .requiredInput(i.getRequiredInput() != null ? i.getRequiredInput() : i.getRequiredInputType())
                .requiredInputType(i.getRequiredInputType())
                .context(i.getContext())
                .resolutionPayload(i.getResolutionPayload())
                .createdAt(i.getCreatedAt())
                .resolvedAt(i.getResolvedAt())
                .build();
    }
}
