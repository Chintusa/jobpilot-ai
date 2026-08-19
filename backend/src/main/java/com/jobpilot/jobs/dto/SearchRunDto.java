package com.jobpilot.jobs.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.jobs.entity.SearchRun;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRunDto {

    private UUID id;
    private String status;
    private List<String> searchStrategies;
    private List<String> roleVariations;
    private List<String> sourcesQueried;
    private String query;
    private int numberFound;
    private int duplicatesRemoved;
    private int filteredJobs;
    private int matchedJobs;
    private int recommendedJobs;
    private String errors;
    private Instant startedAt;
    private Instant completedAt;
    private long durationMs;
    private String auditLog;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static SearchRunDto fromEntity(SearchRun r) {
        if (r == null) return null;
        return SearchRunDto.builder()
                .id(r.getId())
                .status(r.getStatus())
                .searchStrategies(parseList(r.getSearchStrategies()))
                .roleVariations(parseList(r.getRoleVariations()))
                .sourcesQueried(parseList(r.getSourcesQueried()))
                .query(r.getQuery())
                .numberFound(r.getNumberFound())
                .duplicatesRemoved(r.getDuplicatesRemoved())
                .filteredJobs(r.getFilteredJobs())
                .matchedJobs(r.getMatchedJobs())
                .recommendedJobs(r.getRecommendedJobs())
                .errors(r.getErrors())
                .startedAt(r.getStartedAt())
                .completedAt(r.getCompletedAt())
                .durationMs(r.getDurationMs())
                .auditLog(r.getAuditLog())
                .build();
    }

    private static List<String> parseList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
