package com.jobpilot.preferences.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.preferences.dto.JobPreferencesDto;
import com.jobpilot.preferences.dto.UpdateJobPreferencesRequest;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPreferencesService {

    private final JobPreferencesRepository preferencesRepository;
    private final com.jobpilot.autoapply.repository.AutoApplyPolicyRepository autoApplyPolicyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public JobPreferencesDto getPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        JobPreferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        return JobPreferencesDto.fromEntity(preferences);
    }

    @Transactional
    public JobPreferencesDto updatePreferences(String userEmail, UpdateJobPreferencesRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        JobPreferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        if (request.getTargetRoles() != null) preferences.setTargetRoles(toJson(request.getTargetRoles()));
        if (request.getRoleVariations() != null) preferences.setRoleVariations(toJson(request.getRoleVariations()));
        if (request.getLocations() != null) preferences.setLocations(toJson(request.getLocations()));
        if (request.getWorkModes() != null) preferences.setWorkModes(toJson(request.getWorkModes()));
        if (request.getMinSalary() != null) preferences.setMinSalary(request.getMinSalary());
        if (request.getMaxSalary() != null) preferences.setMaxSalary(request.getMaxSalary());
        if (request.getMinExperience() != null) preferences.setMinExperience(request.getMinExperience());
        if (request.getMaxExperience() != null) preferences.setMaxExperience(request.getMaxExperience());
        if (request.getIndustries() != null) preferences.setIndustries(toJson(request.getIndustries()));
        if (request.getRequiredSkills() != null) preferences.setRequiredSkills(toJson(request.getRequiredSkills()));
        if (request.getPreferredSkills() != null) preferences.setPreferredSkills(toJson(request.getPreferredSkills()));
        if (request.getExcludedCompanies() != null) preferences.setExcludedCompanies(toJson(request.getExcludedCompanies()));
        if (request.getExcludedKeywords() != null) preferences.setExcludedKeywords(toJson(request.getExcludedKeywords()));
        if (request.getJobTypes() != null) preferences.setJobTypes(toJson(request.getJobTypes()));

        if (request.getAutoApplyEnabled() != null) preferences.setAutoApplyEnabled(request.getAutoApplyEnabled());
        if (request.getAutoApplyMinScore() != null) preferences.setAutoApplyMinScore(request.getAutoApplyMinScore());
        if (request.getAutoApplyDailyLimit() != null) preferences.setAutoApplyDailyLimit(request.getAutoApplyDailyLimit());
        if (request.getRequireApproval() != null) preferences.setRequireApproval(request.getRequireApproval());

        JobPreferences saved = preferencesRepository.save(preferences);

        // Synchronize with AutoApplyPolicy table
        autoApplyPolicyRepository.findByUserId(user.getId()).ifPresentOrElse(policy -> {
            policy.setEnabled(saved.isAutoApplyEnabled());
            policy.setMinimumScore(saved.getAutoApplyMinScore());
            policy.setRequireApproval(saved.isRequireApproval());
            policy.setMaxApplicationsPerDay(saved.getAutoApplyDailyLimit());
            autoApplyPolicyRepository.save(policy);
        }, () -> {
            com.jobpilot.autoapply.entity.AutoApplyPolicy policy = com.jobpilot.autoapply.entity.AutoApplyPolicy.builder()
                    .user(user)
                    .enabled(saved.isAutoApplyEnabled())
                    .minimumScore(saved.getAutoApplyMinScore())
                    .requireApproval(saved.isRequireApproval())
                    .maxApplicationsPerDay(saved.getAutoApplyDailyLimit())
                    .build();
            autoApplyPolicyRepository.save(policy);
        });

        log.info("Updated job search & auto-apply preferences for user: {}", userEmail);
        return JobPreferencesDto.fromEntity(saved);
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private JobPreferences createDefaultPreferences(User user) {
        JobPreferences p = JobPreferences.builder()
                .user(user)
                .targetRoles("[\"Java Backend Developer\", \"Spring Boot Engineer\"]")
                .roleVariations("[\"Backend Engineer\", \"Java Developer\", \"Spring Boot Developer\"]")
                .locations("[\"Bengaluru, India\", \"Remote\"]")
                .workModes("[\"HYBRID\", \"REMOTE\"]")
                .minSalary(BigDecimal.valueOf(600000.00))
                .maxSalary(BigDecimal.valueOf(1800000.00))
                .minExperience(BigDecimal.valueOf(1.0))
                .maxExperience(BigDecimal.valueOf(5.0))
                .industries("[\"Fintech\", \"SaaS\", \"Enterprise Software\"]")
                .requiredSkills("[\"Java\", \"Spring Boot\", \"REST APIs\", \"SQL\"]")
                .preferredSkills("[\"Docker\", \"AWS\", \"Redis\", \"Kafka\"]")
                .excludedCompanies("[\"Revature\"]")
                .excludedKeywords("[\"Unpaid\", \"Senior Director\"]")
                .jobTypes("[\"FULL_TIME\"]")
                .autoApplyEnabled(false)
                .autoApplyMinScore(85)
                .autoApplyDailyLimit(5)
                .requireApproval(true)
                .build();
        return preferencesRepository.save(p);
    }
}
