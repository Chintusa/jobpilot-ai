package com.jobpilot.candidate.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidate_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_title")
    private String currentTitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "total_experience_years", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal totalExperienceYears = BigDecimal.ZERO;

    @Column(name = "target_salary_min", precision = 12, scale = 2)
    private BigDecimal targetSalaryMin;

    @Column(name = "target_salary_max", precision = 12, scale = 2)
    private BigDecimal targetSalaryMax;

    private String location;

    @Column(name = "preferred_work_mode")
    @Builder.Default
    private String preferredWorkMode = "HYBRID";

    @Column(name = "completeness_score")
    @Builder.Default
    private Integer completenessScore = 0;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean isApproved = false;

    @Column(name = "projects_json", columnDefinition = "TEXT")
    private String projectsJson;

    @Column(name = "certifications_json", columnDefinition = "TEXT")
    private String certificationsJson;

    @Column(name = "achievements_json", columnDefinition = "TEXT")
    private String achievementsJson;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileEducation> educations = new ArrayList<>();
}
