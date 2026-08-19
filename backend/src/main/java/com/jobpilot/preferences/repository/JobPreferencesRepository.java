package com.jobpilot.preferences.repository;

import com.jobpilot.preferences.entity.JobPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPreferencesRepository extends JpaRepository<JobPreferences, UUID> {
    Optional<JobPreferences> findByUserId(UUID userId);
}
