package com.jobpilot.autoapply.dto;

import com.jobpilot.autoapply.entity.AutoApplyPolicy;
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
public class AutoApplyPolicyDto {
    private UUID id;
    private UUID userId;
    private boolean enabled;
    private Integer minimumScore;
    private boolean requireApproval;
    private Integer maxApplicationsPerDay;
    private Instant createdAt;
    private Instant updatedAt;

    public static AutoApplyPolicyDto fromEntity(AutoApplyPolicy policy) {
        if (policy == null) {
            return AutoApplyPolicyDto.builder()
                    .enabled(false)
                    .minimumScore(85)
                    .requireApproval(true)
                    .maxApplicationsPerDay(5)
                    .build();
        }
        return AutoApplyPolicyDto.builder()
                .id(policy.getId())
                .userId(policy.getUser() != null ? policy.getUser().getId() : null)
                .enabled(policy.isEnabled())
                .minimumScore(policy.getMinimumScore())
                .requireApproval(policy.isRequireApproval())
                .maxApplicationsPerDay(policy.getMaxApplicationsPerDay())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
