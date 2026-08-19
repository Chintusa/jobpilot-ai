package com.jobpilot.applications.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.applications.entity.Application;
import com.jobpilot.applications.entity.ScreeningQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {

    private UUID id;
    private UUID jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String status;
    private String preparationState;
    private String applicationSummary;
    private List<String> missingInformation;
    private String tailoredResumeContent;
    private String tailoredResumeUrl;
    private String coverLetter;
    private String submissionMethod;
    private String sourceName;
    private String submissionResult;
    private String failureReason;
    private Instant appliedAt;
    private Instant withdrawnAt;
    private Instant interviewAt;
    private Instant offerAt;
    private Instant rejectedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private int eventsCount;
    private List<ScreeningQuestionDto> screeningQuestions;
    private List<ApplicationEventDto> timeline;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreeningQuestionDto {
        private UUID id;
        private String question;
        private String aiAnswer;
        private String candidateAnswer;
        private String confidence;
        private String source;
        private String status;
    }

    public static ApplicationDto fromEntity(Application a) {
        if (a == null) return null;
        return ApplicationDto.builder()
                .id(a.getId())
                .jobId(a.getJob() != null ? a.getJob().getId() : null)
                .jobTitle(a.getJob() != null ? a.getJob().getTitle() : null)
                .company(a.getJob() != null ? a.getJob().getCompany() : null)
                .location(a.getJob() != null ? a.getJob().getLocation() : null)
                .status(a.getStatus())
                .preparationState(a.getPreparationState())
                .applicationSummary(a.getApplicationSummary())
                .missingInformation(parseList(a.getMissingInformation()))
                .tailoredResumeContent(a.getTailoredResumeContent())
                .tailoredResumeUrl(a.getTailoredResumeUrl())
                .coverLetter(a.getCoverLetter())
                .submissionMethod(a.getSubmissionMethod())
                .sourceName(a.getSourceName())
                .submissionResult(a.getSubmissionResult())
                .failureReason(a.getFailureReason())
                .appliedAt(a.getAppliedAt())
                .withdrawnAt(a.getWithdrawnAt())
                .interviewAt(a.getInterviewAt())
                .offerAt(a.getOfferAt())
                .rejectedAt(a.getRejectedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .eventsCount(a.getEvents() != null ? a.getEvents().size() : 0)
                .screeningQuestions(a.getScreeningQuestions() != null ? a.getScreeningQuestions().stream().map(q -> ScreeningQuestionDto.builder()
                        .id(q.getId())
                        .question(q.getQuestion())
                        .aiAnswer(q.getAiAnswer())
                        .candidateAnswer(q.getCandidateAnswer())
                        .confidence(q.getConfidence())
                        .source(q.getSource())
                        .status(q.getStatus())
                        .build()).collect(Collectors.toList()) : List.of())
                .timeline(a.getEvents() != null ? a.getEvents().stream().map(ApplicationEventDto::fromEntity).collect(Collectors.toList()) : List.of())
                .build();
    }

    private static List<String> parseList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
