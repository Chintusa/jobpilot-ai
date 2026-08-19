package com.jobpilot.jobs.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileSkill;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.dto.SearchRunDto;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import com.jobpilot.jobs.entity.SearchRun;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.jobs.repository.JobSourceRepository;
import com.jobpilot.jobs.repository.SearchRunRepository;
import com.jobpilot.jobs.source.ExternalJob;
import com.jobpilot.jobs.source.JobNormalizer;
import com.jobpilot.jobs.source.JobSearchCriteria;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.service.MatchingEngineService;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchAgentService {

    private final List<com.jobpilot.jobs.source.JobSource> jobSources;
    private final JobNormalizer jobNormalizer;
    private final JobRepository jobRepository;
    private final JobSourceRepository jobSourceRepository;
    private final SearchRunRepository searchRunRepository;
    private final MatchingEngineService matchingEngineService;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobPreferencesRepository jobPreferencesRepository;
    private final ObjectMapper objectMapper;
    private final com.jobpilot.config.MetricsService metricsService;

    @Transactional
    public SearchRunDto executeAutonomousSearchRun(String userEmail) {
        long startTime = System.currentTimeMillis();
        Instant startInstant = Instant.now();
        log.info("Initiating 9-Step Autonomous AI Job Search Agent Run for user: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId()).orElse(null);
        JobPreferences preferences = jobPreferencesRepository.findByUserId(user.getId()).orElse(null);

        List<String> auditLogs = new ArrayList<>();
        auditLogs.add("[Step 0] Agent initialized for candidate " + userEmail + " at " + startInstant);

        // ——— Step 1: Generate Search Strategies ———
        List<String> searchStrategies = generateSearchStrategies(profile, preferences);
        auditLogs.add("[Step 1: Strategies] Generated " + searchStrategies.size() + " targeted search strategies: " + searchStrategies);

        // ——— Step 2: Generate Role Variations ———
        List<String> roleVariations = generateRoleVariations(profile, preferences);
        auditLogs.add("[Step 2: Role Variations] Formulated " + roleVariations.size() + " role variations: " + roleVariations);

        // ——— Step 3: Search Supported Job Sources ———
        List<String> sourcesQueried = new ArrayList<>();
        List<ExternalJob> rawDiscovered = new ArrayList<>();

        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword(searchStrategies.isEmpty() ? "Java" : searchStrategies.get(0))
                .targetRoles(roleVariations)
                .limit(30)
                .build();

        for (com.jobpilot.jobs.source.JobSource source : jobSources) {
            String sourceName = source.getSourceName();
            sourcesQueried.add(sourceName);
            metricsService.incrementJobSearchRun(sourceName);
            try {
                List<ExternalJob> found = source.search(criteria);
                rawDiscovered.addAll(found);
                auditLogs.add("[Step 3: Source Search] Queried source '" + sourceName + "' -> Discovered " + found.size() + " raw postings.");
            } catch (Exception e) {
                metricsService.incrementSourceFailure(sourceName, e.getClass().getSimpleName());
                auditLogs.add("[Step 3: Error] Error querying source " + sourceName + ": " + e.getMessage());
            }
        }

        // ——— Step 4 & 5: Normalize and Deduplicate Results ———
        int numberFound = rawDiscovered.size();
        int duplicatesRemoved = 0;
        int filteredCount = 0;
        List<Job> persistedJobs = new ArrayList<>();

        List<String> excludedCompanies = preferences != null ? parseList(preferences.getExcludedCompanies()) : List.of();
        List<String> excludedKeywords = preferences != null ? parseList(preferences.getExcludedKeywords()) : List.of();
        List<String> preferredLocations = preferences != null ? parseList(preferences.getLocations()) : List.of();

        for (ExternalJob raw : rawDiscovered) {
            // Step 6: Apply Hard Eligibility Filters (Exclusions)
            if (isExcluded(raw.getRawCompany(), excludedCompanies)) {
                filteredCount++;
                auditLogs.add("[Step 6: Filtered] Excluded job from blacklisted company: " + raw.getRawCompany());
                continue;
            }

            if (hasExcludedKeyword(raw.getRawTitle(), raw.getRawDescription(), excludedKeywords)) {
                filteredCount++;
                auditLogs.add("[Step 6: Filtered] Excluded job with blacklisted keyword: " + raw.getRawTitle());
                continue;
            }

            if (!preferredLocations.isEmpty()) {
                boolean matchesLocOrRemote = "Remote".equalsIgnoreCase(raw.getRawWorkMode()) ||
                        preferredLocations.stream().anyMatch(l -> raw.getRawLocation() != null && raw.getRawLocation().toLowerCase().contains(l.toLowerCase()));
                if (!matchesLocOrRemote) {
                    filteredCount++;
                    auditLogs.add("[Step 6: Filtered] Excluded job outside preferred locations: " + raw.getRawLocation());
                    continue;
                }
            }

            // Step 4: Normalization
            JobSource sourceEntity = getOrCreateSourceEntity(raw.getSourceName());
            Job normalized = jobNormalizer.normalize(raw, sourceEntity);

            // Step 5: Multi-layer Deduplication
            Optional<Job> byExternal = jobRepository.findBySourceIdAndExternalId(sourceEntity.getId(), raw.getExternalId());
            if (byExternal.isPresent()) {
                persistedJobs.add(byExternal.get());
                duplicatesRemoved++;
                continue;
            }

            if (normalized.getCanonicalUrl() != null) {
                Optional<Job> byCanonical = jobRepository.findByCanonicalUrl(normalized.getCanonicalUrl());
                if (byCanonical.isPresent()) {
                    persistedJobs.add(byCanonical.get());
                    duplicatesRemoved++;
                    continue;
                }
            }

            if (normalized.getDedupHash() != null) {
                Optional<Job> byHash = jobRepository.findByDedupHash(normalized.getDedupHash());
                if (byHash.isPresent()) {
                    persistedJobs.add(byHash.get());
                    duplicatesRemoved++;
                    continue;
                }
            }

            Job saved = jobRepository.save(normalized);
            persistedJobs.add(saved);
        }

        auditLogs.add("[Step 4 & 5] Normalization & Deduplication: " + numberFound + " raw found, " + duplicatesRemoved + " dupes skipped, " + persistedJobs.size() + " unique jobs processed.");

        // ——— Step 7 & 8: Calculate Match Scores & Rank Jobs ———
        List<JobMatchDto> calculatedMatches = new ArrayList<>();
        int matchedJobs = 0;
        int recommendedJobs = 0;

        for (Job job : persistedJobs) {
            JobMatchDto match = matchingEngineService.calculateOrGetMatch(user.getEmail(), job.getId());
            calculatedMatches.add(match);

            if (match.getOverallScore() != null && match.getOverallScore() >= 70) {
                matchedJobs++;
            }
            if ("APPLY".equalsIgnoreCase(match.getRecommendation())) {
                recommendedJobs++;
            }
        }

        // Sort by match score descending
        calculatedMatches.sort((a, b) -> Integer.compare(
                b.getOverallScore() != null ? b.getOverallScore() : 0,
                a.getOverallScore() != null ? a.getOverallScore() : 0
        ));

        auditLogs.add("[Step 7 & 8: Scoring & Ranking] Evaluated " + calculatedMatches.size() + " jobs across 8 recruiter categories. Matched (≥70%): " + matchedJobs + ".");

        // ——— Step 9: Generate Recommendations ———
        auditLogs.add("[Step 9: Recommendations] Generated " + recommendedJobs + " APPLY recommendations and " + (matchedJobs - recommendedJobs) + " REVIEW recommendations.");

        long durationMs = System.currentTimeMillis() - startTime;
        Instant completedInstant = Instant.now();
        auditLogs.add("[Completion] Autonomous run successfully completed in " + durationMs + "ms.");

        // Record SearchRun Audit Record
        SearchRun run = SearchRun.builder()
                .user(user)
                .status("COMPLETED")
                .searchStrategies(toJson(searchStrategies))
                .roleVariations(toJson(roleVariations))
                .sourcesQueried(toJson(sourcesQueried))
                .query(searchStrategies.isEmpty() ? "Java Backend" : searchStrategies.get(0))
                .numberFound(numberFound)
                .duplicatesRemoved(duplicatesRemoved)
                .filteredJobs(filteredCount)
                .matchedJobs(matchedJobs)
                .recommendedJobs(recommendedJobs)
                .startedAt(startInstant)
                .completedAt(completedInstant)
                .durationMs(durationMs)
                .auditLog(String.join("\n", auditLogs))
                .build();

        SearchRun savedRun = searchRunRepository.save(run);
        log.info("Completed SearchRun id={} for user={}, found={}, matched={}, recommended={} (duration: {}ms)",
                savedRun.getId(), userEmail, numberFound, matchedJobs, recommendedJobs, durationMs);

        return SearchRunDto.fromEntity(savedRun);
    }

    @Transactional(readOnly = true)
    public List<SearchRunDto> getSearchRunsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return searchRunRepository.findTop10ByUserIdOrderByStartedAtDesc(user.getId())
                .stream()
                .map(SearchRunDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SearchRunDto getSearchRunById(String userEmail, UUID runId) {
        SearchRun run = searchRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Search run not found: " + runId));
        return SearchRunDto.fromEntity(run);
    }

    private List<String> generateSearchStrategies(CandidateProfile profile, JobPreferences preferences) {
        List<String> strategies = new ArrayList<>();
        if (preferences != null) {
            List<String> targetRoles = parseList(preferences.getTargetRoles());
            if (!targetRoles.isEmpty()) strategies.addAll(targetRoles);
        }

        if (profile != null) {
            if (profile.getCurrentTitle() != null && !strategies.contains(profile.getCurrentTitle())) {
                strategies.add(profile.getCurrentTitle());
            }
            if (profile.getSkills() != null) {
                String topSkills = profile.getSkills().stream()
                        .limit(3)
                        .map(ProfileSkill::getName)
                        .collect(Collectors.joining(" "));
                if (!topSkills.isBlank()) strategies.add(topSkills + " Engineer");
            }
        }

        if (strategies.isEmpty()) {
            strategies.add("Java Backend Developer");
            strategies.add("Spring Boot Microservices Engineer");
        }

        return strategies.stream().distinct().collect(Collectors.toList());
    }

    private List<String> generateRoleVariations(CandidateProfile profile, JobPreferences preferences) {
        List<String> variations = new ArrayList<>();
        if (preferences != null) {
            List<String> roleVars = parseList(preferences.getRoleVariations());
            if (!roleVars.isEmpty()) variations.addAll(roleVars);
        }

        variations.add("Java Developer");
        variations.add("Backend Software Engineer");
        variations.add("Spring Boot Engineer");
        variations.add("Cloud Backend Architect");
        variations.add("Distributed Systems Engineer");

        return variations.stream().distinct().collect(Collectors.toList());
    }

    private JobSource getOrCreateSourceEntity(String sourceName) {
        String name = sourceName != null ? sourceName : "MODULAR_SOURCE";
        return jobSourceRepository.findByName(name)
                .orElseGet(() -> jobSourceRepository.save(JobSource.builder()
                        .name(name)
                        .enabled(true)
                        .adapterClass("com.jobpilot.jobs.source.MockJobSource")
                        .lastSyncAt(Instant.now())
                        .build()));
    }

    private boolean isExcluded(String company, List<String> excludedList) {
        if (company == null || excludedList == null || excludedList.isEmpty()) return false;
        return excludedList.stream().anyMatch(ex -> company.toLowerCase().contains(ex.toLowerCase()));
    }

    private boolean hasExcludedKeyword(String title, String desc, List<String> excluded) {
        if (excluded == null || excluded.isEmpty()) return false;
        String combined = ((title != null ? title : "") + " " + (desc != null ? desc : "")).toLowerCase();
        return excluded.stream().anyMatch(kw -> combined.contains(kw.toLowerCase()));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
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
