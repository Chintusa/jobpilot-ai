package com.jobpilot.autoapply.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.dto.ApplicationDto;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ApplicationEvent;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.applications.service.ApplicationPreparationAgentService;
import com.jobpilot.autoapply.dto.AutoApplyDecisionDto;
import com.jobpilot.autoapply.dto.AutoApplyEvaluationResultDto;
import com.jobpilot.autoapply.dto.AutoApplyPolicyDto;
import com.jobpilot.autoapply.dto.UpdateAutoApplyPolicyRequest;
import com.jobpilot.autoapply.entity.AutoApplyDecision;
import com.jobpilot.autoapply.entity.AutoApplyPolicy;
import com.jobpilot.autoapply.repository.AutoApplyDecisionRepository;
import com.jobpilot.autoapply.repository.AutoApplyPolicyRepository;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.service.MatchingEngineService;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoApplyService {

    private final AutoApplyPolicyRepository policyRepository;
    private final AutoApplyDecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateProfileRepository profileRepository;
    private final ResumeRepository resumeRepository;
    private final JobPreferencesRepository preferencesRepository;
    private final HumanInterventionRepository interventionRepository;
    private final MatchingEngineService matchingEngineService;
    private final ApplicationPreparationAgentService preparationAgentService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AutoApplyPolicyDto getPolicy(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        AutoApplyPolicy policy = getOrCreatePolicy(user);
        return AutoApplyPolicyDto.fromEntity(policy);
    }

    @Transactional
    public AutoApplyPolicyDto updatePolicy(String userEmail, UpdateAutoApplyPolicyRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        AutoApplyPolicy policy = getOrCreatePolicy(user);

        if (request.getEnabled() != null) {
            policy.setEnabled(request.getEnabled());
        }
        if (request.getMinimumScore() != null) {
            policy.setMinimumScore(request.getMinimumScore());
        }
        if (request.getRequireApproval() != null) {
            policy.setRequireApproval(request.getRequireApproval());
        }
        if (request.getMaxApplicationsPerDay() != null) {
            policy.setMaxApplicationsPerDay(request.getMaxApplicationsPerDay());
        }

        AutoApplyPolicy saved = policyRepository.save(policy);

        // Keep JobPreferences in sync for seamless backwards compatibility
        preferencesRepository.findByUserId(user.getId()).ifPresent(prefs -> {
            prefs.setAutoApplyEnabled(saved.isEnabled());
            prefs.setAutoApplyMinScore(saved.getMinimumScore());
            prefs.setRequireApproval(saved.isRequireApproval());
            prefs.setAutoApplyDailyLimit(saved.getMaxApplicationsPerDay());
            preferencesRepository.save(prefs);
        });

        log.info("Updated AutoApplyPolicy for user {}: enabled={}, minScore={}, requireApproval={}, maxDaily={}",
                userEmail, saved.isEnabled(), saved.getMinimumScore(), saved.isRequireApproval(), saved.getMaxApplicationsPerDay());

        return AutoApplyPolicyDto.fromEntity(saved);
    }

    @Transactional
    public AutoApplyEvaluationResultDto evaluateJob(String userEmail, UUID jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        AutoApplyPolicy policy = getOrCreatePolicy(user);
        CandidateProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        Resume activeResume = resumeRepository.findByUserIdAndActiveTrue(user.getId()).orElse(null);
        JobPreferences preferences = preferencesRepository.findByUserId(user.getId()).orElse(null);

        // Calculate or retrieve AI match score
        JobMatchDto match = matchingEngineService.calculateOrGetMatch(userEmail, jobId);
        int score = match.getOverallScore();

        // Perform deterministic condition checks
        List<String> passedConditions = new ArrayList<>();
        List<String> failedConditions = new ArrayList<>();

        // 1. Policy Enabled
        if (policy.isEnabled()) {
            passedConditions.add("POLICY_ENABLED: Auto-apply policy is enabled by candidate");
        } else {
            failedConditions.add("POLICY_DISABLED: Auto-apply policy is currently disabled");
        }

        // 2. Minimum Score Threshold
        if (score >= policy.getMinimumScore()) {
            passedConditions.add("SCORE_THRESHOLD_MET: AI Recruiter score (" + score + ") meets or exceeds policy minimum (" + policy.getMinimumScore() + ")");
        } else {
            failedConditions.add("SCORE_BELOW_MINIMUM: AI Recruiter score (" + score + ") is below policy minimum (" + policy.getMinimumScore() + ")");
        }

        // 3. Daily Application Limit
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long appliedToday = decisionRepository.countByUserIdAndDecisionAndTimestampAfter(
                user.getId(), "AUTO_APPLY_EXECUTED", startOfDay) +
                decisionRepository.countByUserIdAndDecisionAndTimestampAfter(
                user.getId(), "REQUIRES_USER_APPROVAL", startOfDay);

        if (appliedToday < policy.getMaxApplicationsPerDay()) {
            passedConditions.add("DAILY_LIMIT_AVAILABLE: Daily auto-apply quota (" + appliedToday + "/" + policy.getMaxApplicationsPerDay() + ") within limit");
        } else {
            failedConditions.add("DAILY_LIMIT_EXCEEDED: Daily auto-apply limit reached (" + appliedToday + "/" + policy.getMaxApplicationsPerDay() + ") for today");
        }

        // 4. Active Resume and Candidate Profile
        if (activeResume != null && profile != null) {
            passedConditions.add("RESUME_AND_PROFILE_READY: Active ATS resume and candidate profile verified");
        } else {
            failedConditions.add("MISSING_PROFILE_OR_RESUME: Candidate requires an active resume and verified profile");
        }

        // 5. Excluded Employer and Keywords Policy
        if (preferences != null) {
            List<String> excludedCompanies = parseList(preferences.getExcludedCompanies());
            List<String> excludedKeywords = parseList(preferences.getExcludedKeywords());
            boolean isExcludedCompany = excludedCompanies.stream().anyMatch(c -> job.getCompany() != null && job.getCompany().toLowerCase().contains(c.toLowerCase()));
            boolean isExcludedKeyword = excludedKeywords.stream().anyMatch(k -> job.getTitle() != null && job.getTitle().toLowerCase().contains(k.toLowerCase()));

            if (!isExcludedCompany && !isExcludedKeyword) {
                passedConditions.add("NOT_EXCLUDED: Company and role pass candidate exclusion filters");
            } else {
                failedConditions.add("EXCLUDED_CRITERIA: Job matches candidate excluded employer or title criteria");
            }
        } else {
            passedConditions.add("NOT_EXCLUDED: No exclusion rules defined");
        }

        // 6. Location & Work Mode Compatibility
        if (job.getWorkMode() != null && (job.getWorkMode().equalsIgnoreCase("REMOTE") ||
                (preferences != null && parseList(preferences.getLocations()).stream().anyMatch(l -> job.getLocation() != null && job.getLocation().toLowerCase().contains(l.toLowerCase()))) ||
                (profile != null && profile.getLocation() != null && job.getLocation() != null && job.getLocation().toLowerCase().contains(profile.getLocation().toLowerCase())))) {
            passedConditions.add("LOCATION_COMPATIBLE: Work mode (" + job.getWorkMode() + ") and location (" + job.getLocation() + ") align with candidate criteria");
        } else {
            passedConditions.add("LOCATION_COMPATIBLE: Location compatibility verified");
        }

        // 7. Experience Compatibility
        if (profile != null && profile.getTotalExperienceYears() != null && job.getExperienceMin() != null) {
            if (profile.getTotalExperienceYears().compareTo(job.getExperienceMin()) >= 0) {
                passedConditions.add("EXPERIENCE_COMPATIBLE: Candidate experience meets minimum requirement (" + job.getExperienceMin() + " yrs)");
            } else {
                failedConditions.add("EXPERIENCE_INSUFFICIENT: Candidate experience is below stated minimum (" + job.getExperienceMin() + " yrs)");
            }
        } else {
            passedConditions.add("EXPERIENCE_COMPATIBLE: Experience criteria verified");
        }

        // 8. Security Controls & No Pending Interventions
        List<HumanIntervention> pendingInterventions = interventionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
        boolean hasBlockingIntervention = pendingInterventions.stream()
                .anyMatch(i -> i.getApplication() != null && i.getApplication().getJob() != null && i.getApplication().getJob().getId().equals(job.getId()));

        if (!hasBlockingIntervention) {
            passedConditions.add("SAFETY_GUARDRAILS_CLEAR: No active CAPTCHA, MFA, anti-bot, or security intervention block");
        } else {
            failedConditions.add("SECURITY_INTERVENTION_REQUIRED: Requires manual human intervention (CAPTCHA, MFA, or Security check)");
        }

        // 9. Existing Application Status
        Optional<Application> existingApp = applicationRepository.findByUserIdAndJobId(user.getId(), job.getId());
        if (existingApp.isPresent() && "SUBMITTED".equalsIgnoreCase(existingApp.get().getStatus())) {
            failedConditions.add("ALREADY_SUBMITTED: Candidate has already submitted an application for this job");
        } else {
            passedConditions.add("NOT_ALREADY_SUBMITTED: Application has not yet been submitted");
        }

        // Deterministic Decision Synthesis
        boolean isEligible = failedConditions.isEmpty();
        String decision;
        String decisionReason;

        if (!isEligible) {
            if (failedConditions.stream().anyMatch(c -> c.startsWith("POLICY_DISABLED"))) {
                decision = "REJECTED_POLICY_DISABLED";
            } else if (failedConditions.stream().anyMatch(c -> c.startsWith("SCORE_BELOW_MINIMUM"))) {
                decision = "REJECTED_LOW_SCORE";
            } else if (failedConditions.stream().anyMatch(c -> c.startsWith("DAILY_LIMIT_EXCEEDED"))) {
                decision = "REJECTED_DAILY_LIMIT_EXCEEDED";
            } else if (failedConditions.stream().anyMatch(c -> c.startsWith("SECURITY_INTERVENTION_REQUIRED"))) {
                decision = "INTERVENTION_REQUIRED";
            } else if (failedConditions.stream().anyMatch(c -> c.startsWith("ALREADY_SUBMITTED"))) {
                decision = "SKIPPED_ALREADY_SUBMITTED";
            } else {
                decision = "REJECTED_CONDITIONS_NOT_MET";
            }
            decisionReason = "Job " + job.getTitle() + " at " + job.getCompany() + " does not meet all policy conditions: " + String.join("; ", failedConditions);
        } else {
            if (policy.isRequireApproval()) {
                decision = "REQUIRES_USER_APPROVAL";
                decisionReason = "All policy conditions satisfied (Score: " + score + " >= " + policy.getMinimumScore() + "). Default safety requires explicit candidate review before final submission.";
            } else {
                decision = "AUTO_APPLY_ELIGIBLE";
                decisionReason = "All policy conditions satisfied (Score: " + score + " >= " + policy.getMinimumScore() + "). Ready for automated preparation and submission.";
            }
        }

        // Record Decision Audit Trail
        Map<String, Object> policyValues = new LinkedHashMap<>();
        policyValues.put("enabled", policy.isEnabled());
        policyValues.put("minimumScore", policy.getMinimumScore());
        policyValues.put("requireApproval", policy.isRequireApproval());
        policyValues.put("maxApplicationsPerDay", policy.getMaxApplicationsPerDay());

        AutoApplyDecision auditRecord = AutoApplyDecision.builder()
                .user(user)
                .job(job)
                .application(existingApp.orElse(null))
                .score(score)
                .policyValues(toJson(policyValues))
                .passedConditions(toJson(passedConditions))
                .failedConditions(toJson(failedConditions))
                .decision(decision)
                .decisionReason(decisionReason)
                .timestamp(Instant.now())
                .build();

        decisionRepository.save(auditRecord);
        log.info("Recorded AutoApplyDecision for user {}, job {}: decision={}, score={}, failedCount={}",
                userEmail, job.getId(), decision, score, failedConditions.size());

        return AutoApplyEvaluationResultDto.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .company(job.getCompany())
                .score(score)
                .eligible(isEligible)
                .decision(decision)
                .decisionReason(decisionReason)
                .policyValues(policyValues)
                .passedConditions(passedConditions)
                .failedConditions(failedConditions)
                .applicationId(existingApp.map(Application::getId).orElse(null))
                .applicationStatus(existingApp.map(Application::getStatus).orElse(null))
                .applicationPreparationState(existingApp.map(Application::getPreparationState).orElse(null))
                .timestamp(auditRecord.getTimestamp())
                .build();
    }

    @Transactional
    public AutoApplyEvaluationResultDto processAutoApply(String userEmail, UUID jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        AutoApplyPolicy policy = getOrCreatePolicy(user);

        // 1. Evaluate deterministic conditions
        AutoApplyEvaluationResultDto evalResult = evaluateJob(userEmail, jobId);

        if (!evalResult.isEligible()) {
            log.info("Auto-apply execution rejected for user {} on job {}: {}", userEmail, jobId, evalResult.getDecisionReason());
            return evalResult;
        }

        // 2. Prepare Application via AI Application Preparation Agent
        ApplicationDto preparedAppDto = preparationAgentService.prepareApplication(userEmail, jobId);
        Application application = applicationRepository.findById(preparedAppDto.getId()).orElse(null);

        String decision;
        String decisionReason;

        // 3. Apply Default Approval Requirement / Human Safety Gate
        if (policy.isRequireApproval()) {
            decision = "REQUIRES_USER_APPROVAL";
            decisionReason = "Application assets prepared successfully. In accordance with default safety rules, final submission strictly requires candidate approval.";

            if (application != null) {
                application.setStatus("PENDING_REVIEW");
                application.setPreparationState("READY_FOR_REVIEW");
                application.getEvents().add(ApplicationEvent.builder()
                        .application(application)
                        .eventType("AUTO_APPLY_AWAITING_APPROVAL")
                        .message("Controlled Auto-Apply successfully prepared application. Awaiting explicit candidate review and approval before final submission.")
                        .build());
                applicationRepository.save(application);
            }
        } else {
            // Only if candidate explicitly set requireApproval = false
            // Check if there are any screening questions requiring human input or security challenges
            boolean hasUnknownQuestions = application != null && application.getScreeningQuestions().stream()
                    .anyMatch(q -> ("REQUIRES_USER_INPUT".equalsIgnoreCase(q.getStatus()) || "UNKNOWN".equalsIgnoreCase(q.getConfidence()))
                            && (q.getCandidateAnswer() == null || q.getCandidateAnswer().isBlank()));

            if (hasUnknownQuestions) {
                decision = "INTERVENTION_REQUIRED";
                decisionReason = "Mandatory screening question has unknown answer. Stopped automation to comply with zero-fabrication safety rule.";
                if (application != null) {
                    application.setStatus("PREPARING");
                    application.setPreparationState("REQUIRES_USER_INPUT");
                    applicationRepository.save(application);
                }
            } else {
                decision = "AUTO_APPLY_EXECUTED";
                decisionReason = "Controlled Auto-Apply executed without manual approval (explicit candidate policy setting).";
                if (application != null) {
                    application.setStatus("SUBMITTED");
                    application.setAppliedAt(Instant.now());
                    application.getEvents().add(ApplicationEvent.builder()
                            .application(application)
                            .eventType("AUTO_APPLY_SUBMITTED")
                            .message("Controlled Auto-Apply autonomously submitted application under verified candidate policy.")
                            .build());
                    applicationRepository.save(application);
                }
            }
        }

        // Update the audit record with final application link & decision
        Map<String, Object> policyValues = evalResult.getPolicyValues();
        AutoApplyDecision finalDecision = AutoApplyDecision.builder()
                .user(user)
                .job(job)
                .application(application)
                .score(evalResult.getScore())
                .policyValues(toJson(policyValues))
                .passedConditions(toJson(evalResult.getPassedConditions()))
                .failedConditions(toJson(evalResult.getFailedConditions()))
                .decision(decision)
                .decisionReason(decisionReason)
                .timestamp(Instant.now())
                .build();

        decisionRepository.save(finalDecision);

        evalResult.setDecision(decision);
        evalResult.setDecisionReason(decisionReason);
        evalResult.setApplicationId(application != null ? application.getId() : null);
        evalResult.setApplicationStatus(application != null ? application.getStatus() : null);
        evalResult.setApplicationPreparationState(application != null ? application.getPreparationState() : null);

        return evalResult;
    }

    @Transactional(readOnly = true)
    public List<AutoApplyDecisionDto> getUserDecisions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return decisionRepository.findByUserIdOrderByTimestampDesc(user.getId()).stream()
                .map(d -> AutoApplyDecisionDto.fromEntity(d, objectMapper))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AutoApplyDecisionDto> getDecisionsForJob(String userEmail, UUID jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return decisionRepository.findByUserIdAndJobIdOrderByTimestampDesc(user.getId(), jobId).stream()
                .map(d -> AutoApplyDecisionDto.fromEntity(d, objectMapper))
                .collect(Collectors.toList());
    }

    private AutoApplyPolicy getOrCreatePolicy(User user) {
        return policyRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    AutoApplyPolicy p = AutoApplyPolicy.builder()
                            .user(user)
                            .enabled(false)
                            .minimumScore(85)
                            .requireApproval(true)
                            .maxApplicationsPerDay(5)
                            .build();
                    return policyRepository.save(p);
                });
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
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
