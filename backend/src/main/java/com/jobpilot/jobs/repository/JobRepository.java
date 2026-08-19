package com.jobpilot.jobs.repository;

import com.jobpilot.jobs.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    Optional<Job> findBySourceIdAndExternalId(UUID sourceId, String externalId);

    Optional<Job> findByCanonicalUrl(String canonicalUrl);

    Optional<Job> findByDedupHash(String dedupHash);
}
