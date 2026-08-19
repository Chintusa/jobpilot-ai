package com.jobpilot.automation.repository;

import com.jobpilot.automation.entity.HumanIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HumanInterventionRepository extends JpaRepository<HumanIntervention, UUID> {
    List<HumanIntervention> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<HumanIntervention> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
}
