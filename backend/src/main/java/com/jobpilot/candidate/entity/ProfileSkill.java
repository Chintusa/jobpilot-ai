package com.jobpilot.candidate.entity;

import com.jobpilot.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "profile_skills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile profile;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String category = "TECHNICAL";

    @Builder.Default
    private String proficiency = "INTERMEDIATE";

    @Column(name = "evidence_type")
    @Builder.Default
    private String evidenceType = "DEMONSTRATED";

    @Column(name = "years_experience", precision = 3, scale = 1)
    private BigDecimal yearsExperience;
}
