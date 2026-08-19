package com.jobpilot.preferences.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "job_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPreferences extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "target_roles", columnDefinition = "TEXT")
    @Builder.Default
    private String targetRoles = "[]";

    @Column(name = "role_variations", columnDefinition = "TEXT")
    @Builder.Default
    private String roleVariations = "[]";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String locations = "[]";

    @Column(name = "work_modes", columnDefinition = "TEXT")
    @Builder.Default
    private String workModes = "[]";

    @Column(name = "min_salary", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minSalary = BigDecimal.ZERO;

    @Column(name = "max_salary", precision = 12, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "min_experience", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal minExperience = BigDecimal.ZERO;

    @Column(name = "max_experience", precision = 3, scale = 1)
    private BigDecimal maxExperience;

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String industries = "[]";

    @Column(name = "required_skills", columnDefinition = "TEXT")
    @Builder.Default
    private String requiredSkills = "[]";

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    @Builder.Default
    private String preferredSkills = "[]";

    @Column(name = "excluded_companies", columnDefinition = "TEXT")
    @Builder.Default
    private String excludedCompanies = "[]";

    @Column(name = "excluded_keywords", columnDefinition = "TEXT")
    @Builder.Default
    private String excludedKeywords = "[]";

    @Column(name = "job_types", columnDefinition = "TEXT")
    @Builder.Default
    private String jobTypes = "[]";

    @Column(name = "auto_apply_enabled", nullable = false)
    @Builder.Default
    private boolean autoApplyEnabled = false;

    @Column(name = "auto_apply_min_score", nullable = false)
    @Builder.Default
    private Integer autoApplyMinScore = 85;

    @Column(name = "auto_apply_daily_limit", nullable = false)
    @Builder.Default
    private Integer autoApplyDailyLimit = 5;

    @Column(name = "require_approval", nullable = false)
    @Builder.Default
    private boolean requireApproval = true;
}
