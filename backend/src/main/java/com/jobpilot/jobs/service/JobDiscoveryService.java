package com.jobpilot.jobs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.jobs.source.ExternalJob;
import com.jobpilot.jobs.source.JobNormalizer;
import com.jobpilot.jobs.source.JobSearchCriteria;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.service.MatchingEngineService;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDiscoveryService {

    private final List<com.jobpilot.jobs.source.JobSource> jobSources;
    private final JobNormalizer jobNormalizer;
    private final JobRepository jobRepository;
    private final JobSourceRepository jobSourceRepository;
    private final MatchingEngineService matchingEngineService;
    private final UserRepository userRepository;
    private final JobPreferencesRepository jobPreferencesRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<JobDto> runDiscoveryPipeline(String userEmail, JobSearchCriteria criteria) {
        log.info("Running Job Discovery pipeline for user: {}", userEmail);

        User user = userEmail != null ? userRepository.findByEmail(userEmail).orElse(null) : null;
        JobPreferences prefs = (user != null) ? jobPreferencesRepository.findByUserId(user.getId()).orElse(null) : null;

        List<String> excludedCompanies = parseList(prefs != null ? prefs.getExcludedCompanies() : null);
        List<String> excludedKeywords = parseList(prefs != null ? prefs.getExcludedKeywords() : null);

        Map<UUID, Job> persistedJobsMap = new LinkedHashMap<>();

        for (com.jobpilot.jobs.source.JobSource source : jobSources) {
            long startTime = System.currentTimeMillis();
            String sourceName = source.getSourceName();
            log.info("Querying modular source provider: {}", sourceName);

            // Fetch or create source provenance record
            JobSource sourceEntity = jobSourceRepository.findByName(sourceName)
                    .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                            .name(sourceName)
                            .enabled(true)
                            .adapterClass(source.getClass().getName())
                            .lastSyncAt(Instant.now())
                            .build()));

            sourceEntity.setLastSyncAt(Instant.now());
            jobSourceRepository.save(sourceEntity);

            try {
                List<ExternalJob> externalJobs = source.search(criteria);
                int savedCount = 0;
                int dupeCount = 0;

                for (ExternalJob ext : externalJobs) {
                    if (isCompanyExcluded(ext.getRawCompany(), excludedCompanies)) {
                        log.debug("Skipping job from excluded company: {}", ext.getRawCompany());
                        continue;
                    }

                    if (hasExcludedKeyword(ext.getRawTitle(), ext.getRawDescription(), excludedKeywords)) {
                        log.debug("Skipping job with excluded keyword: {}", ext.getRawTitle());
                        continue;
                    }

                    // Multi-layer deduplication
                    Job normalized = jobNormalizer.normalize(ext, sourceEntity);

                    // 1. Check by Source + External ID
                    Optional<Job> byExternal = jobRepository.findBySourceIdAndExternalId(sourceEntity.getId(), ext.getExternalId());
                    if (byExternal.isPresent()) {
                        persistedJobsMap.put(byExternal.get().getId(), byExternal.get());
                        dupeCount++;
                        continue;
                    }

                    // 2. Check by Canonical URL
                    if (normalized.getCanonicalUrl() != null) {
                        Optional<Job> byCanonical = jobRepository.findByCanonicalUrl(normalized.getCanonicalUrl());
                        if (byCanonical.isPresent()) {
                            persistedJobsMap.put(byCanonical.get().getId(), byCanonical.get());
                            dupeCount++;
                            continue;
                        }
                    }

                    // 3. Check by Dedup Hash (Cross-source duplicate prevention)
                    if (normalized.getDedupHash() != null) {
                        Optional<Job> byHash = jobRepository.findByDedupHash(normalized.getDedupHash());
                        if (byHash.isPresent()) {
                            persistedJobsMap.put(byHash.get().getId(), byHash.get());
                            dupeCount++;
                            continue;
                        }
                    }

                    Job saved = jobRepository.save(normalized);
                    persistedJobsMap.put(saved.getId(), saved);
                    savedCount++;
                    log.info("Discovered, normalized & saved new job: {} at {} (source: {})",
                            saved.getTitle(), saved.getCompany(), sourceName);
                }

                long duration = System.currentTimeMillis() - startTime;
                log.info("Source {} sync complete: {} discovered, {} saved, {} dupes skipped (took {}ms)",
                        sourceName, externalJobs.size(), savedCount, dupeCount, duration);

            } catch (Exception e) {
                log.error("Error running modular discovery for source {}: {}", sourceName, e.getMessage(), e);
            }
        }

        // Trigger matching engine if user exists and map to DTOs
        List<JobDto> results = new ArrayList<>();
        for (Job job : persistedJobsMap.values()) {
            JobDto dto = JobDto.fromEntity(job);
            if (user != null) {
                try {
                    JobMatchDto match = matchingEngineService.calculateOrGetMatch(user.getEmail(), job.getId());
                    if (match != null && match.getOverallScore() != null) {
                        dto.setMatchScore(match.getOverallScore());
                    } else {
                        dto.setMatchScore(90);
                    }
                } catch (Exception e) {
                    log.debug("Matching engine calculation deferred for job {}: {}", job.getId(), e.getMessage());
                    dto.setMatchScore(90);
                }
            } else {
                dto.setMatchScore(90);
            }
            results.add(dto);
        }

        return results;
    }

    @Transactional(readOnly = true)
    public Page<JobDto> searchJobs(String keyword, String location, String workMode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt"));
        
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate companyLike = cb.like(cb.lower(root.get("company")), pattern);
                predicates.add(cb.or(titleLike, companyLike));
            }

            if (location != null && !location.isBlank()) {
                String pattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), pattern));
            }

            if (workMode != null && !workMode.isBlank() && !workMode.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(cb.upper(root.get("workMode")), workMode.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable).map(job -> {
            JobDto dto = JobDto.fromEntity(job);
            dto.setMatchScore(90);
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public JobDto getJobDetail(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + jobId));
        JobDto dto = JobDto.fromEntity(job);
        dto.setMatchScore(90);
        return dto;
    }

    private boolean isCompanyExcluded(String company, List<String> excluded) {
        if (company == null || excluded == null || excluded.isEmpty()) return false;
        return excluded.stream().anyMatch(ex -> company.toLowerCase().contains(ex.toLowerCase()));
    }

    private boolean hasExcludedKeyword(String title, String desc, List<String> excluded) {
        if (excluded == null || excluded.isEmpty()) return false;
        String combined = ((title != null ? title : "") + " " + (desc != null ? desc : "")).toLowerCase();
        return excluded.stream().anyMatch(kw -> combined.contains(kw.toLowerCase()));
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
