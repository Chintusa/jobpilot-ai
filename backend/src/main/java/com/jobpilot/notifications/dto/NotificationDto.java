package com.jobpilot.notifications.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.notifications.entity.Notification;
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
public class NotificationDto {

    private UUID id;
    private String title;
    private String message;
    private String type;
    private String category;
    private String channel;
    private boolean read;
    private Instant readAt;
    private boolean emailDelivered;
    private String actionUrl;
    private UUID referenceId;
    private String referenceType;
    private Map<String, Object> metadata;
    private Instant createdAt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static NotificationDto fromEntity(Notification n) {
        if (n == null) return null;

        Map<String, Object> metaMap = Map.of();
        if (n.getMetadata() != null && !n.getMetadata().isBlank() && !n.getMetadata().equals("{}")) {
            try {
                metaMap = objectMapper.readValue(n.getMetadata(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception ignored) {}
        }

        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .category(n.getCategory())
                .channel(n.getChannel())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .emailDelivered(n.isEmailDelivered())
                .actionUrl(n.getActionUrl())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .metadata(metaMap)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
