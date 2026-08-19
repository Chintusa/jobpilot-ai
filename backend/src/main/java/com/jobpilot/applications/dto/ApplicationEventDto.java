package com.jobpilot.applications.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.entity.ApplicationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEventDto {
    private UUID id;
    private UUID applicationId;
    private String eventType;
    private String fromStatus;
    private String toStatus;
    private String source;
    private String message;
    private Map<String, Object> metadata;
    private Instant createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ApplicationEventDto fromEntity(ApplicationEvent event) {
        if (event == null) return null;

        Map<String, Object> metaMap = Map.of();
        if (event.getMetadata() != null && !event.getMetadata().isBlank()) {
            try {
                metaMap = objectMapper.readValue(event.getMetadata(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {}
        }

        return ApplicationEventDto.builder()
                .id(event.getId())
                .applicationId(event.getApplication() != null ? event.getApplication().getId() : null)
                .eventType(event.getEventType())
                .fromStatus(event.getFromStatus())
                .toStatus(event.getToStatus())
                .source(event.getSource())
                .message(event.getMessage())
                .metadata(metaMap)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
