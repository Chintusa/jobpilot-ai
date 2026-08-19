package com.jobpilot.applications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationContentRequest {

    private String coverLetter;
    private String tailoredResumeContent;
    private Boolean userApproved;
    private List<ScreeningAnswerUpdate> screeningAnswers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreeningAnswerUpdate {
        private UUID questionId;
        private String candidateAnswer;
        private String status; // ACCEPTED, EDITED, NEEDS_ANSWER
    }
}
