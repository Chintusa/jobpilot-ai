package com.jobpilot.automation.dto;

import com.jobpilot.automation.entity.AgentSchedule;
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
public class AgentStatusDto {

    private UUID id;
    private String status; // ACTIVE, PAUSED, BUSY
    private boolean enabled;
    private String cronExpression;
    private Instant lastRunAt;
    private Instant nextRunAt;

    public static AgentStatusDto fromEntity(AgentSchedule s) {
        if (s == null) return null;
        return AgentStatusDto.builder()
                .id(s.getId())
                .status(s.getStatus())
                .enabled(s.isEnabled())
                .cronExpression(s.getCronExpression())
                .lastRunAt(s.getLastRunAt())
                .nextRunAt(s.getNextRunAt())
                .build();
    }
}
