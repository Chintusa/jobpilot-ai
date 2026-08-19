package com.jobpilot.applications.repository;

import com.jobpilot.applications.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Application> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
    Optional<Application> findByUserIdAndJobId(UUID userId, UUID jobId);
    long countByUserId(UUID userId);
    long countByUserIdAndStatus(UUID userId, String status);
}
