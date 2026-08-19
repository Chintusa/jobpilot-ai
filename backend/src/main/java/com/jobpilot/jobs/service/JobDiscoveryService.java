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
import com.jobpilot.jobs.source.JobSourceResult;
import com.jobpilot.matching.service.MatchingEngineService;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        User user = userRepository.findByEmail(userEmail).orElse(null);
        JobPreferences prefs = (user != null) ? jobPreferencesRepository.findByUserId(user.getId()).orElse(null) : null;

        List<String> excludedCompanies = parseList(prefs != null ? prefs.getExcludedCompanies() : null);
        List<String> excludedKeywords = parseList(prefs != null ? prefs.getExcludedKeywords() : null);

        List<Job> persistedJobs = new ArrayList<>();

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
                        persistedJobs.add(byExternal.get());
                        dupeCount++;
                        continue;
                    }

                    // 2. Check by Canonical URL
                    if (normalized.getCanonicalUrl() != null) {
                        Optional<Job> byCanonical = jobRepository.findByCanonicalUrl(normalized.getCanonicalUrl());
                        if (byCanonical.isPresent()) {
                            persistedJobs.add(byCanonical.get());
                            dupeCount++;
                            continue;
                        }
                    }

                    // 3. Check by Dedup Hash (Cross-source duplicate prevention)
                    if (normalized.getDedupHash() != null) {
                        Optional<Job> byHash = jobRepository.findByDedupHash(normalized.getDedupHash());
                        if (byHash.isPresent()) {
                            persistedJobs.add(byHash.get());
                            dupeCount++;
                            continue;
                        }
                    }

                    Job saved = jobRepository.save(normalized);
                    persistedJobs.add(saved);
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

        // Trigger matching engine if user exists
        if (user != null) {
            for (Job job : persistedJobs) {
                matchingEngineService.calculateOrGetMatch(user.getEmail(), job.getId());
            }
        }

        return persistedJobs.stream().map(job -> {
            JobDto dto = JobDto.fromEntity(job);
            dto.setMatchScore(92);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobDto> searchJobs(String keyword, String location, String workMode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt"));
        Page<Job> jobPage = jobRepository.searchJobs(keyword, location, workMode, pageable);
        return jobPage.map(JobDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public JobDto getJobDetail(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found with id: " + jobId));
        return JobDto.fromEntity(job);
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
