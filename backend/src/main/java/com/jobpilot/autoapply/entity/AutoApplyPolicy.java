package com.jobpilot.autoapply.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auto_apply_policies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoApplyPolicy extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "minimum_score", nullable = false)
    @Builder.Default
    private Integer minimumScore = 85;

    @Column(name = "require_approval", nullable = false)
    @Builder.Default
    private boolean requireApproval = true;

    @Column(name = "max_applications_per_day", nullable = false)
    @Builder.Default
    private Integer maxApplicationsPerDay = 5;
}
