package com.jobpilot.autoapply.repository;

import com.jobpilot.autoapply.entity.AutoApplyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutoApplyPolicyRepository extends JpaRepository<AutoApplyPolicy, UUID> {
    Optional<AutoApplyPolicy> findByUserId(UUID userId);
}
