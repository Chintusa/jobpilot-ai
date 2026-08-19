package com.jobpilot.preferences.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.preferences.entity.JobPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPreferencesDto {

    private UUID id;
    private List<String> targetRoles;
    private List<String> roleVariations;
    private List<String> locations;
    private List<String> workModes;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal minExperience;
    private BigDecimal maxExperience;
    private List<String> industries;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private List<String> excludedCompanies;
    private List<String> excludedKeywords;
    private List<String> jobTypes;
    private boolean autoApplyEnabled;
    private Integer autoApplyMinScore;
    private Integer autoApplyDailyLimit;
    private boolean requireApproval;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JobPreferencesDto fromEntity(JobPreferences p) {
        if (p == null) return null;
        return JobPreferencesDto.builder()
                .id(p.getId())
                .targetRoles(parseList(p.getTargetRoles()))
                .roleVariations(parseList(p.getRoleVariations()))
                .locations(parseList(p.getLocations()))
                .workModes(parseList(p.getWorkModes()))
                .minSalary(p.getMinSalary())
                .maxSalary(p.getMaxSalary())
                .minExperience(p.getMinExperience())
                .maxExperience(p.getMaxExperience())
                .industries(parseList(p.getIndustries()))
                .requiredSkills(parseList(p.getRequiredSkills()))
                .preferredSkills(parseList(p.getPreferredSkills()))
                .excludedCompanies(parseList(p.getExcludedCompanies()))
                .excludedKeywords(parseList(p.getExcludedKeywords()))
                .jobTypes(parseList(p.getJobTypes()))
                .autoApplyEnabled(p.isAutoApplyEnabled())
                .autoApplyMinScore(p.getAutoApplyMinScore())
                .autoApplyDailyLimit(p.getAutoApplyDailyLimit())
                .requireApproval(p.isRequireApproval())
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
