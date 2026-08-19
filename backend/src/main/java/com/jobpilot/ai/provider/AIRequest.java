package com.jobpilot.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String prompt;
    private String systemMessage;
    private String taskType; // EXTRACT_PROFILE, GENERATE_COVER_LETTER, SCREENING_QUESTION
    private Map<String, Object> parameters;
}
