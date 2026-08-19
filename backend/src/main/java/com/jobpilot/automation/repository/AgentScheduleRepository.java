package com.jobpilot.automation.repository;

import com.jobpilot.automation.entity.AgentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentScheduleRepository extends JpaRepository<AgentSchedule, UUID> {
    Optional<AgentSchedule> findByUserId(UUID userId);
}
