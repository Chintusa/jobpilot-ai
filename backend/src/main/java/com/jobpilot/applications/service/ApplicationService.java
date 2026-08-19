package com.jobpilot.applications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.dto.*;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ApplicationEvent;
import com.jobpilot.applications.entity.ApplicationStatus;
import com.jobpilot.applications.entity.ScreeningQuestion;
import com.jobpilot.applications.repository.ApplicationEventRepository;
import com.jobpilot.applications.repository.ApplicationRepository;
import com.jobpilot.applications.repository.ScreeningQuestionRepository;
import com.jobpilot.automation.entity.HumanIntervention;
import com.jobpilot.automation.repository.HumanInterventionRepository;
import com.jobpilot.common.exception.BadRequestException;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
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
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationEventRepository eventRepository;
    private final ScreeningQuestionRepository screeningQuestionRepository;
    private final HumanInterventionRepository interventionRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;
    private final com.jobpilot.config.MetricsService metricsService;

    @Transactional(readOnly = true)
    public List<ApplicationDto> getUserApplications(String userEmail, String status) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Application> apps = (status != null && !status.equalsIgnoreCase("ALL"))
                ? applicationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status.toUpperCase())
                : applicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return apps.stream().map(ApplicationDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getFilteredApplications(String userEmail, ApplicationFilterRequest filter) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Application> apps = applicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        if (filter == null) {
            return apps.stream().map(ApplicationDto::fromEntity).collect(Collectors.toList());
        }

        return apps.stream()
                .filter(a -> {
                    // Status filter
                    if (filter.getStatus() != null && !filter.getStatus().isBlank() && !filter.getStatus().equalsIgnoreCase("ALL")) {
                        if (!a.getStatus().equalsIgnoreCase(filter.getStatus())) return false;
                    }
                    // Source filter
                    if (filter.getSource() != null && !filter.getSource().isBlank() && !filter.getSource().equalsIgnoreCase("ALL")) {
                        if (a.getSourceName() == null || !a.getSourceName().equalsIgnoreCase(filter.getSource())) return false;
                    }
                    // Company filter
                    if (filter.getCompany() != null && !filter.getCompany().isBlank()) {
                        if (a.getJob() == null || a.getJob().getCompany() == null ||
                                !a.getJob().getCompany().toLowerCase().contains(filter.getCompany().toLowerCase())) return false;
                    }
                    // Query keyword (job title or company)
                    if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
                        String q = filter.getQuery().toLowerCase();
                        boolean titleMatch = a.getJob() != null && a.getJob().getTitle() != null && a.getJob().getTitle().toLowerCase().contains(q);
                        boolean companyMatch = a.getJob() != null && a.getJob().getCompany() != null && a.getJob().getCompany().toLowerCase().contains(q);
                        if (!titleMatch && !companyMatch) return false;
                    }
                    // Start date
                    if (filter.getStartDate() != null) {
                        if (a.getCreatedAt().isBefore(filter.getStartDate())) return false;
                    }
                    // End date
                    if (filter.getEndDate() != null) {
                        if (a.getCreatedAt().isAfter(filter.getEndDate())) return false;
                    }
                    return true;
                })
                .sorted((a1, a2) -> {
                    boolean desc = filter.getSortDirection() == null || filter.getSortDirection().equalsIgnoreCase("DESC");
                    int cmp;
                    if ("appliedAt".equalsIgnoreCase(filter.getSortBy())) {
                        Instant t1 = a1.getAppliedAt() != null ? a1.getAppliedAt() : a1.getCreatedAt();
                        Instant t2 = a2.getAppliedAt() != null ? a2.getAppliedAt() : a2.getCreatedAt();
                        cmp = t1.compareTo(t2);
                    } else if ("company".equalsIgnoreCase(filter.getSortBy())) {
                        String c1 = a1.getJob() != null ? a1.getJob().getCompany() : "";
                        String c2 = a2.getJob() != null ? a2.getJob().getCompany() : "";
                        cmp = c1.compareToIgnoreCase(c2);
                    } else {
                        cmp = a1.getCreatedAt().compareTo(a2.getCreatedAt());
                    }
                    return desc ? -cmp : cmp;
                })
                .map(ApplicationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationDto getApplicationDetail(String userEmail, UUID applicationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not authorized to access application: " + applicationId);
        }

        return ApplicationDto.fromEntity(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationEventDto> getApplicationTimeline(String userEmail, UUID applicationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not authorized to access application timeline: " + applicationId);
        }

        List<ApplicationEvent> events = eventRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
        return events.stream().map(ApplicationEventDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public ApplicationDto updateApplicationStatus(String userEmail, UUID applicationId, UpdateApplicationStatusRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not authorized to modify application: " + applicationId);
        }

        String oldStatus = app.getStatus() != null ? app.getStatus() : "DRAFT";
        String requestedStatus = request.getStatus();
        ApplicationStatus statusEnum = ApplicationStatus.fromString(requestedStatus);
        String canonicalStatus = statusEnum.name();
        app.setStatus(canonicalStatus);

        Instant now = Instant.now();
        switch (canonicalStatus) {
            case "DRAFT", "PREPARING" -> app.setPreparationState("PREPARING");
            case "REQUIRES_USER_INPUT" -> app.setPreparationState("REQUIRES_USER_INPUT");
            case "READY_FOR_REVIEW" -> app.setPreparationState("READY_FOR_REVIEW");
            case "APPROVED", "SUBMITTING" -> app.setPreparationState("USER_APPROVED");
            case "SUBMITTED" -> {
                if (app.getAppliedAt() == null) app.setAppliedAt(now);
                app.setPreparationState("SUBMITTED");
            }
            case "FAILED" -> {
                if (request.getNote() != null) app.setFailureReason(request.getNote());
            }
            case "WITHDRAWN" -> app.setWithdrawnAt(now);
            case "INTERVIEW" -> app.setInterviewAt(now);
            case "OFFER" -> app.setOfferAt(now);
            case "REJECTED" -> app.setRejectedAt(now);
        }

        String note = (request.getNote() != null && !request.getNote().isBlank())
                ? request.getNote()
                : "Application status transition from " + oldStatus + " to " + canonicalStatus;

        String source = request.getSource() != null ? request.getSource() : "CANDIDATE_USER";
        String metaJson = request.getMetadata() != null ? toJson(request.getMetadata()) : "{}";

        app.addEvent("STATUS_CHANGED", oldStatus, canonicalStatus, source, note, metaJson);

        Application saved = applicationRepository.save(app);
        log.info("Transitioned application {} status: {} -> {} by {}", applicationId, oldStatus, canonicalStatus, userEmail);

        return ApplicationDto.fromEntity(saved);
    }

    @Transactional
    public ApplicationDto recordWorkerResult(String userEmail, UUID applicationId, WorkerCallbackRequest callback) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not authorized to update application worker result");
        }

        String oldStatus = app.getStatus();
        String workerStatus = callback.getStatus() != null ? callback.getStatus().toUpperCase() : "FAILED";

        Map<String, Object> metaMap = new LinkedHashMap<>();
        if (callback.getScreenshots() != null) metaMap.put("screenshots", callback.getScreenshots());
        if (callback.getLogs() != null) metaMap.put("logs", callback.getLogs());
        if (callback.getExecutionTimeMs() != null) metaMap.put("executionTimeMs", callback.getExecutionTimeMs());

        metricsService.incrementApplicationExecution(workerStatus);

        if ("SUBMITTED".equalsIgnoreCase(workerStatus) || "SUCCESS".equalsIgnoreCase(workerStatus)) {
            app.setStatus("SUBMITTED");
            app.setPreparationState("SUBMITTED");
            app.setAppliedAt(Instant.now());
            if (callback.getSubmissionResult() != null) {
                app.setSubmissionResult(callback.getSubmissionResult());
            }

            metaMap.put("submissionResult", callback.getSubmissionResult());
            app.addEvent("SUBMISSION_SUCCESS", oldStatus, "SUBMITTED", "APPLICATION_WORKER",
                    "Browser automation worker successfully submitted application to employer portal", toJson(metaMap));
        } else if ("HUMAN_INTERVENTION_REQUIRED".equalsIgnoreCase(workerStatus)) {
            app.setStatus("PREPARING");
            app.setPreparationState("REQUIRES_USER_INPUT");

            String reason = callback.getInterventionReason() != null ? callback.getInterventionReason() : "UNSUPPORTED_FLOW";
            String desc = callback.getInterventionDescription() != null ? callback.getInterventionDescription() : "Automated submission halted; manual action required";

            // Create HumanIntervention record
            HumanIntervention intervention = HumanIntervention.builder()
                    .user(user)
                    .application(app)
                    .reason(reason)
                    .type(reason)
                    .description(desc)
                    .status("PENDING")
                    .requiredInput("CAPTCHA".equalsIgnoreCase(reason) || "MFA".equalsIgnoreCase(reason) ? "SECURITY_VERIFICATION" : "TEXT")
                    .requiredInputType("CAPTCHA".equalsIgnoreCase(reason) || "MFA".equalsIgnoreCase(reason) ? "SECURITY_VERIFICATION" : "TEXT")
                    .context(toJson(metaMap))
                    .build();
            interventionRepository.save(intervention);

            metaMap.put("interventionReason", reason);
            metaMap.put("interventionDescription", desc);
            app.addEvent("HUMAN_INTERVENTION_TRIGGERED", oldStatus, "REQUIRES_USER_INPUT", "APPLICATION_WORKER",
                    "Automation paused: " + reason + " - " + desc, toJson(metaMap));
        } else {
            app.setStatus("FAILED");
            String failureReason = callback.getFailureReason() != null ? callback.getFailureReason() : "Browser automation execution error";
            app.setFailureReason(failureReason);
            
            metricsService.incrementWorkerFailure(failureReason);

            metaMap.put("failureReason", failureReason);
            app.addEvent("SUBMISSION_FAILED", oldStatus, "FAILED", "APPLICATION_WORKER",
                    "Application submission failed: " + failureReason, toJson(metaMap));
        }

        Application saved = applicationRepository.save(app);
        log.info("Recorded worker callback for application {}: resultStatus={}, oldStatus={}",
                applicationId, workerStatus, oldStatus);

        return ApplicationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationStatisticsDto getApplicationStatistics(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Application> apps = applicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        long total = apps.size();
        long preparing = 0;
        long readyForReview = 0;
        long submitted = 0;
        long interview = 0;
        long offer = 0;
        long rejected = 0;
        long withdrawn = 0;
        long failed = 0;

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        Map<String, Long> sourceBreakdown = new LinkedHashMap<>();

        for (Application a : apps) {
            String s = a.getStatus().toUpperCase();
            statusBreakdown.put(s, statusBreakdown.getOrDefault(s, 0L) + 1L);

            String src = a.getSourceName() != null ? a.getSourceName() : "JOBPILOT_DIRECT";
            sourceBreakdown.put(src, sourceBreakdown.getOrDefault(src, 0L) + 1L);

            switch (s) {
                case "PREPARING", "DRAFT", "MATCHED" -> preparing++;
                case "READY_FOR_REVIEW", "PENDING_REVIEW", "APPROVED" -> readyForReview++;
                case "SUBMITTED", "SUBMITTING" -> submitted++;
                case "INTERVIEW" -> interview++;
                case "OFFER" -> offer++;
                case "REJECTED" -> rejected++;
                case "WITHDRAWN" -> withdrawn++;
                case "FAILED" -> failed++;
            }
        }

        long activeFunnelSubmitted = submitted + interview + offer;
        double interviewRate = activeFunnelSubmitted > 0 ? (double) (interview + offer) / activeFunnelSubmitted * 100.0 : 0.0;
        double offerRate = activeFunnelSubmitted > 0 ? (double) offer / activeFunnelSubmitted * 100.0 : 0.0;
        double successRate = total > 0 ? (double) activeFunnelSubmitted / total * 100.0 : 0.0;

        return ApplicationStatisticsDto.builder()
                .totalApplications(total)
                .preparingCount(preparing)
                .readyForReviewCount(readyForReview)
                .submittedCount(submitted)
                .interviewCount(interview)
                .offerCount(offer)
                .rejectedCount(rejected)
                .withdrawnCount(withdrawn)
                .failedCount(failed)
                .interviewRate(Math.round(interviewRate * 10.0) / 10.0)
                .offerRate(Math.round(offerRate * 10.0) / 10.0)
                .successRate(Math.round(successRate * 10.0) / 10.0)
                .statusBreakdown(statusBreakdown)
                .sourceBreakdown(sourceBreakdown)
                .build();
    }

    @Transactional
    public ApplicationDto prepareApplication(String userEmail, UUID jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        return applicationRepository.findByUserIdAndJobId(user.getId(), job.getId())
                .map(ApplicationDto::fromEntity)
                .orElseGet(() -> {
                    Resume activeResume = resumeRepository.findByUserIdAndActiveTrue(user.getId()).orElse(null);

                    Application app = Application.builder()
                            .user(user)
                            .job(job)
                            .resume(activeResume)
                            .status("PREPARING")
                            .preparationState("PREPARING")
                            .tailoredResumeUrl("https://storage.jobpilot.ai/tailored/" + UUID.randomUUID() + "/Tailored_Java_Resume_" + job.getCompany().replaceAll("\\s+", "") + ".pdf")
                            .coverLetter("Dear Hiring Team at " + job.getCompany() + ",\n\nI am thrilled to submit my tailored application for the " + job.getTitle() + " role.")
                            .sourceName(job.getSource() != null ? job.getSource().getName() : "JOBPILOT_DIRECT")
                            .build();

                    // Seed screening questions
                    List<ScreeningQuestion> questions = new ArrayList<>();
                    questions.add(ScreeningQuestion.builder()
                            .application(app)
                            .question("How many years of Java and Spring Boot experience do you have?")
                            .aiAnswer("2.5 years of production experience")
                            .confidence("HIGH")
                            .source("Based on verified candidate profile")
                            .status("ACCEPTED")
                            .build());

                    questions.add(ScreeningQuestion.builder()
                            .application(app)
                            .question("Are you comfortable working in a Hybrid work model in Bengaluru?")
                            .aiAnswer("Yes, comfortable with Hybrid model in Bengaluru")
                            .confidence("HIGH")
                            .source("Based on verified location preferences")
                            .status("ACCEPTED")
                            .build());

                    app.setScreeningQuestions(questions);
                    app.addEvent("APPLICATION_INITIALIZED", "DRAFT", "PREPARING", "AI_AGENT",
                            "AI Agent initiated preparation and generated tailored application assets", "{}");

                    Application saved = applicationRepository.save(app);
                    return ApplicationDto.fromEntity(saved);
                });
    }

    @Transactional
    public ApplicationDto submitApplication(String userEmail, UUID applicationId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Not authorized to submit application: " + applicationId);
        }

        String oldStatus = app.getStatus();
        app.setStatus("SUBMITTED");
        app.setPreparationState("SUBMITTED");
        app.setAppliedAt(Instant.now());

        app.addEvent("APPLICATION_SUBMITTED", oldStatus, "SUBMITTED", "CANDIDATE_USER",
                "Application successfully confirmed by candidate and dispatched", "{}");

        Application saved = applicationRepository.save(app);
        return ApplicationDto.fromEntity(saved);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
