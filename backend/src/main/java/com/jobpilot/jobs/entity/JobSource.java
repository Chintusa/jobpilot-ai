package com.jobpilot.jobs.entity;

import com.jobpilot.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "job_sources")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSource extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "adapter_class", nullable = false)
    private String adapterClass;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;
}
