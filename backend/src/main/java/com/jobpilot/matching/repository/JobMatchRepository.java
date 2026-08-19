package com.jobpilot.matching.repository;

import com.jobpilot.matching.entity.JobMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, UUID> {
    Optional<JobMatch> findByUserIdAndJobId(UUID userId, UUID jobId);
    List<JobMatch> findByUserIdOrderByOverallScoreDesc(UUID userId);
}
