package com.jobpilot.ai.provider;

import com.jobpilot.ai.dto.ExtractedCandidateProfileJson;

public interface AIProvider {

    AIResponse generate(AIRequest request);

    String getProviderName();

    ExtractedCandidateProfileJson extractCandidateProfile(String resumeText);

    String generateCoverLetter(String candidateSummary, String jobDescription);

    String generateScreeningAnswer(String question, String candidateContext);
}
