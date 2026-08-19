package com.jobpilot.applications.repository;

import com.jobpilot.applications.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, UUID> {
    List<ApplicationEvent> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);
}
