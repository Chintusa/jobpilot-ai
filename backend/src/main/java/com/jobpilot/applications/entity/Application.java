package com.jobpilot.applications.entity;

import com.jobpilot.common.entity.BaseEntity;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PREPARING"; // DRAFT, PREPARING, READY_FOR_REVIEW, APPROVED, SUBMITTING, SUBMITTED, FAILED, REQUIRES_USER_INPUT, WITHDRAWN, REJECTED, INTERVIEW, OFFER

    @Column(name = "preparation_state", nullable = false)
    @Builder.Default
    private String preparationState = "PREPARING"; // PREPARING, READY_FOR_REVIEW, REQUIRES_USER_INPUT, USER_APPROVED, SUBMITTED

    @Column(name = "application_summary", columnDefinition = "TEXT")
    private String applicationSummary;

    @Column(name = "missing_information", columnDefinition = "TEXT")
    @Builder.Default
    private String missingInformation = "[]";

    @Column(name = "tailored_resume_content", columnDefinition = "TEXT")
    private String tailoredResumeContent;

    @Column(name = "tailored_resume_url", length = 1024)
    private String tailoredResumeUrl;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "submission_method")
    @Builder.Default
    private String submissionMethod = "AI_AGENT";

    @Column(name = "source_name")
    @Builder.Default
    private String sourceName = "JOBPILOT_DIRECT";

    @Column(name = "submission_result", columnDefinition = "TEXT")
    private String submissionResult;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    @Column(name = "interview_at")
    private Instant interviewAt;

    @Column(name = "offer_at")
    private Instant offerAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ScreeningQuestion> screeningQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ApplicationEvent> events = new ArrayList<>();

    public void addEvent(String eventType, String fromStatus, String toStatus, String source, String message, String metadata) {
        ApplicationEvent event = ApplicationEvent.builder()
                .application(this)
                .eventType(eventType)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .source(source != null ? source : "AI_AGENT")
                .message(message)
                .metadata(metadata != null ? metadata : "{}")
                .build();
        this.events.add(event);
    }
}
