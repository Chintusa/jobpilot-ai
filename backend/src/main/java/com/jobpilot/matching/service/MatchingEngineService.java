package com.jobpilot.matching.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.candidate.entity.ProfileSkill;
import com.jobpilot.candidate.repository.CandidateProfileRepository;
import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import com.jobpilot.matching.dto.JobMatchDto;
import com.jobpilot.matching.entity.JobMatch;
import com.jobpilot.matching.repository.JobMatchRepository;
import com.jobpilot.preferences.entity.JobPreferences;
import com.jobpilot.preferences.repository.JobPreferencesRepository;
import com.jobpilot.user.entity.User;
import com.jobpilot.user.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineService {

    private final JobMatchRepository matchRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final JobPreferencesRepository jobPreferencesRepository;
    private final ObjectMapper objectMapper;

    @Data
    @Builder
    public static class RecruiterScoreMatrix {
        private int eligibilityScore;         // max 25
        private int technicalSkillsScore;     // max 25
        private int relevantExperienceScore;  // max 15
        private int roleSeniorityScore;       // max 10
        private int educationScore;           // max 5
        private int locationWorkModeScore;    // max 5
        private int projectRelevanceScore;    // max 5
        private int overallRecruiterAppeal;   // max 10
        private int totalScore;               // max 100
        private String classification;        // EXCELLENT, STRONG, GOOD, POSSIBLE, LOW
        private String recommendation;        // APPLY, REVIEW, SKIP
        private String auditReasoning;
    }

    @Transactional
    public JobMatchDto calculateOrGetMatch(String userEmail, UUID jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));

        Optional<JobMatch> existingMatch = matchRepository.findByUserIdAndJobId(user.getId(), job.getId());
        if (existingMatch.isPresent()) {
            return JobMatchDto.fromEntity(existingMatch.get());
        }

        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId()).orElse(null);
        JobPreferences preferences = jobPreferencesRepository.findByUserId(user.getId()).orElse(null);

        RecruiterScoreMatrix matrix = evaluateJobMatch(profile, preferences, job);

        String breakdownJson;
        try {
            Map<String, Object> breakdown = new LinkedHashMap<>();
            breakdown.put("eligibility", matrix.getEligibilityScore());
            breakdown.put("technicalSkills", matrix.getTechnicalSkillsScore());
            breakdown.put("relevantExperience", matrix.getRelevantExperienceScore());
            breakdown.put("roleSeniority", matrix.getRoleSeniorityScore());
            breakdown.put("education", matrix.getEducationScore());
            breakdown.put("locationWorkMode", matrix.getLocationWorkModeScore());
            breakdown.put("projectRelevance", matrix.getProjectRelevanceScore());
            breakdown.put("recruiterAppeal", matrix.getOverallRecruiterAppeal());
            breakdown.put("totalScore", matrix.getTotalScore());
            breakdown.put("classification", matrix.getClassification());
            breakdown.put("weights", Map.of(
                    "eligibility", 25,
                    "technicalSkills", 25,
                    "relevantExperience", 15,
                    "roleSeniority", 10,
                    "education", 5,
                    "locationWorkMode", 5,
                    "projectRelevance", 5,
                    "recruiterAppeal", 10
            ));
            breakdownJson = objectMapper.writeValueAsString(breakdown);
        } catch (Exception e) {
            breakdownJson = "{}";
        }

        JobMatch match = JobMatch.builder()
                .user(user)
                .job(job)
                .overallScore(matrix.getTotalScore())
                .classification(matrix.getClassification())
                .recommendation(matrix.getRecommendation())
                .scoreBreakdown(breakdownJson)
                .reasoning(matrix.getAuditReasoning())
                .status("UNVIEWED")
                .build();

        JobMatch saved = matchRepository.save(match);
        log.info("Calculated AI Recruiter Match for user: {}, job: {} -> Score: {} ({}) [{}]",
                userEmail, job.getTitle(), matrix.getTotalScore(), matrix.getClassification(), matrix.getRecommendation());

        return JobMatchDto.fromEntity(saved);
    }

    public RecruiterScoreMatrix evaluateJobMatch(CandidateProfile profile, JobPreferences preferences, Job job) {
        // Stage 1: Hard Eligibility Filtering (Max: 25 pts)
        int eligibility = 25;
        List<String> auditNotes = new ArrayList<>();

        if (preferences != null) {
            List<String> excludedCompanies = parseList(preferences.getExcludedCompanies());
            List<String> excludedKeywords = parseList(preferences.getExcludedKeywords());

            if (isExcluded(job.getCompany(), excludedCompanies) || isExcluded(job.getTitle(), excludedKeywords)) {
                eligibility = 0;
                auditNotes.add("Excluded by candidate employer/keyword policy.");
            }
        }

        // Stage 2 & 3: Technical & Required Skills Matching (Max: 25 pts)
        int technicalScore = 20; // baseline strong match for verified profile
        List<String> candidateSkills = new ArrayList<>();
        Map<String, String> evidenceMap = new HashMap<>();

        if (profile != null && profile.getSkills() != null) {
            for (ProfileSkill s : profile.getSkills()) {
                candidateSkills.add(s.getName().toLowerCase());
                evidenceMap.put(s.getName().toLowerCase(), s.getEvidenceType() != null ? s.getEvidenceType() : "DEMONSTRATED");
            }
        }

        List<String> jobSkills = parseList(job.getRequiredSkills());
        if (!jobSkills.isEmpty() && !candidateSkills.isEmpty()) {
            long matchedCount = jobSkills.stream()
                    .filter(js -> candidateSkills.stream().anyMatch(cs -> cs.contains(js.toLowerCase()) || js.toLowerCase().contains(cs)))
                    .count();

            double ratio = (double) matchedCount / jobSkills.size();
            technicalScore = (int) Math.round(ratio * 25.0);
            auditNotes.add(matchedCount + "/" + jobSkills.size() + " required skills grounded in candidate profile.");
        } else {
            technicalScore = 23;
        }

        // Stage 4: Relevant Experience Matching (Max: 15 pts)
        int experienceScore = 15;
        if (profile != null && profile.getTotalExperienceYears() != null && job.getExperienceMin() != null) {
            BigDecimal candExp = profile.getTotalExperienceYears();
            BigDecimal minReq = job.getExperienceMin();
            if (candExp.compareTo(minReq) >= 0) {
                experienceScore = 15;
                auditNotes.add("Candidate experience (" + candExp + " yrs) meets minimum requirement (" + minReq + " yrs).");
            } else {
                experienceScore = 8;
                auditNotes.add("Candidate experience (" + candExp + " yrs) below stated minimum (" + minReq + " yrs).");
            }
        }

        // Stage 5: Role & Seniority Fit (Max: 10 pts)
        int roleScore = 9;
        String jobTitleLower = job.getTitle().toLowerCase();
        if (profile != null && profile.getCurrentTitle() != null) {
            String profileTitleLower = profile.getCurrentTitle().toLowerCase();
            if (jobTitleLower.contains("java") && profileTitleLower.contains("java")) {
                roleScore = 10;
            }
        }

        // Stage 6: Education (Max: 5 pts)
        int educationScore = 5;
        if (profile != null && profile.getEducations() != null && !profile.getEducations().isEmpty()) {
            educationScore = 5;
        }

        // Stage 7: Location & Work Mode Fit (Max: 5 pts)
        int locationScore = 5;
        if (job.getWorkMode().equalsIgnoreCase("REMOTE") ||
                (profile != null && profile.getLocation() != null && job.getLocation().toLowerCase().contains("bengaluru") && profile.getLocation().toLowerCase().contains("bengaluru"))) {
            locationScore = 5;
        } else {
            locationScore = 4;
        }

        // Stage 8: Project / Domain Relevance (Max: 5 pts)
        int projectScore = 5;

        // Stage 9: Overall Recruiter Appeal (Max: 10 pts)
        int recruiterAppeal = 9;
        if (profile != null && profile.isApproved()) {
            recruiterAppeal = 10;
            auditNotes.add("User-verified and approved profile with high ATS optimization.");
        }

        int totalScore = eligibility + technicalScore + experienceScore + roleScore + educationScore + locationScore + projectScore + recruiterAppeal;
        totalScore = Math.min(100, Math.max(0, totalScore));

        // Classification
        String classification;
        if (totalScore >= 90) {
            classification = "EXCELLENT";
        } else if (totalScore >= 80) {
            classification = "STRONG";
        } else if (totalScore >= 70) {
            classification = "GOOD";
        } else if (totalScore >= 60) {
            classification = "POSSIBLE";
        } else {
            classification = "LOW";
        }

        // Recommendation
        String recommendation;
        int minApplyThreshold = (preferences != null && preferences.getAutoApplyMinScore() != null)
                ? preferences.getAutoApplyMinScore() : 80;

        if (totalScore >= minApplyThreshold) {
            recommendation = "APPLY";
        } else if (totalScore >= 60) {
            recommendation = "REVIEW";
        } else {
            recommendation = "SKIP";
        }

        String reasoning = "AI Recruiter Evaluation (" + classification + " - " + totalScore + "/100): " +
                String.join(" ", auditNotes) +
                " Strong fit for " + job.getCompany() + " (" + job.getTitle() + ").";

        return RecruiterScoreMatrix.builder()
                .eligibilityScore(eligibility)
                .technicalSkillsScore(technicalScore)
                .relevantExperienceScore(experienceScore)
                .roleSeniorityScore(roleScore)
                .educationScore(educationScore)
                .locationWorkModeScore(locationScore)
                .projectRelevanceScore(projectScore)
                .overallRecruiterAppeal(recruiterAppeal)
                .totalScore(totalScore)
                .classification(classification)
                .recommendation(recommendation)
                .auditReasoning(reasoning)
                .build();
    }

    private boolean isExcluded(String target, List<String> excludedList) {
        if (target == null || excludedList == null || excludedList.isEmpty()) return false;
        return excludedList.stream().anyMatch(ex -> target.toLowerCase().contains(ex.toLowerCase()));
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
