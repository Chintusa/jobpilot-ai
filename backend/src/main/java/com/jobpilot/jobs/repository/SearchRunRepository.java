package com.jobpilot.jobs.repository;

import com.jobpilot.jobs.entity.SearchRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchRunRepository extends JpaRepository<SearchRun, UUID> {

    List<SearchRun> findByUserIdOrderByStartedAtDesc(UUID userId);

    List<SearchRun> findTop10ByUserIdOrderByStartedAtDesc(UUID userId);
}
