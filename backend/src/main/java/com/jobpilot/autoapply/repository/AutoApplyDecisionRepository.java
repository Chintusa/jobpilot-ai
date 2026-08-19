package com.jobpilot.autoapply.repository;

import com.jobpilot.autoapply.entity.AutoApplyDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AutoApplyDecisionRepository extends JpaRepository<AutoApplyDecision, UUID> {
    List<AutoApplyDecision> findByUserIdOrderByTimestampDesc(UUID userId);
    List<AutoApplyDecision> findByUserIdAndJobIdOrderByTimestampDesc(UUID userId, UUID jobId);
    long countByUserIdAndDecisionAndTimestampAfter(UUID userId, String decision, Instant after);
}
