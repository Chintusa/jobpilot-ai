package com.jobpilot.autoapply.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.autoapply.entity.AutoApplyDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoApplyDecisionDto {
    private UUID id;
    private UUID userId;
    private UUID jobId;
    private String jobTitle;
    private String company;
    private UUID applicationId;
    private Integer score;
    private Map<String, Object> policyValues;
    private List<String> passedConditions;
    private List<String> failedConditions;
    private String decision;
    private String decisionReason;
    private Instant timestamp;

    public static AutoApplyDecisionDto fromEntity(AutoApplyDecision decision, ObjectMapper objectMapper) {
        if (decision == null) return null;

        Map<String, Object> policyMap = Map.of();
        List<String> passed = List.of();
        List<String> failed = List.of();

        if (objectMapper != null) {
            try {
                if (decision.getPolicyValues() != null) {
                    policyMap = objectMapper.readValue(decision.getPolicyValues(), new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception ignored) {}

            try {
                if (decision.getPassedConditions() != null) {
                    passed = objectMapper.readValue(decision.getPassedConditions(), new TypeReference<List<String>>() {});
                }
            } catch (Exception ignored) {}

            try {
                if (decision.getFailedConditions() != null) {
                    failed = objectMapper.readValue(decision.getFailedConditions(), new TypeReference<List<String>>() {});
                }
            } catch (Exception ignored) {}
        }

        return AutoApplyDecisionDto.builder()
                .id(decision.getId())
                .userId(decision.getUser() != null ? decision.getUser().getId() : null)
                .jobId(decision.getJob() != null ? decision.getJob().getId() : null)
                .jobTitle(decision.getJob() != null ? decision.getJob().getTitle() : null)
                .company(decision.getJob() != null ? decision.getJob().getCompany() : null)
                .applicationId(decision.getApplication() != null ? decision.getApplication().getId() : null)
                .score(decision.getScore())
                .policyValues(policyMap)
                .passedConditions(passed)
                .failedConditions(failed)
                .decision(decision.getDecision())
                .decisionReason(decision.getDecisionReason())
                .timestamp(decision.getTimestamp())
                .build();
    }
}
