package com.jobpilot.applications.adapter;

import com.jobpilot.applications.entity.Application;
import com.jobpilot.candidate.entity.CandidateProfile;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationContext {
    private UUID applicationId;
    private Job job;
    private Application application;
    private CandidateProfile candidateProfile;
    private Resume resume;
    private String tailoredResumeUrl;
    private String coverLetter;
    private Map<String, String> screeningAnswers;
    private Map<String, Object> metadata;
}
