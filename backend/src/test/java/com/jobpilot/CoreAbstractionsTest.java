package com.jobpilot;

import com.jobpilot.ai.provider.AIProvider;
import com.jobpilot.ai.provider.AIRequest;
import com.jobpilot.ai.provider.AIResponse;
import com.jobpilot.ai.provider.StandardStructuredAiProvider;
import com.jobpilot.applications.adapter.ApplicationAdapter;
import com.jobpilot.applications.adapter.ApplicationContext;
import com.jobpilot.applications.adapter.ApplicationResult;
import com.jobpilot.applications.adapter.MockApplicationAdapter;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.source.ExternalJob;
import com.jobpilot.jobs.source.JobSearchCriteria;
import com.jobpilot.jobs.source.JobSource;
import com.jobpilot.jobs.source.MockJobSource;
import com.jobpilot.resume.storage.FileStorageService;
import com.jobpilot.resume.storage.FileUpload;
import com.jobpilot.resume.storage.LocalStorageService;
import com.jobpilot.resume.storage.StoredFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreAbstractionsTest {

    @Test
    @DisplayName("JobSource abstraction should perform search and provide source name")
    void testJobSourceAbstraction() {
        JobSource jobSource = new MockJobSource();
        assertThat(jobSource.getSourceName()).isEqualTo(MockJobSource.SOURCE_NAME);

        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .limit(5)
                .build();

        List<ExternalJob> results = jobSource.search(criteria);
        assertThat(results).isNotEmpty();
        assertThat(results.getFirst().getRawTitle()).containsIgnoringCase("Java");
    }

    @Test
    @DisplayName("ApplicationAdapter abstraction should verify support and execute application")
    void testApplicationAdapterAbstraction() {
        ApplicationAdapter adapter = new MockApplicationAdapter();
        Job job = Job.builder().title("Staff Engineer").company("Acme").build();

        assertThat(adapter.supports(job)).isTrue();

        UUID appId = UUID.randomUUID();
        ApplicationContext context = ApplicationContext.builder()
                .applicationId(appId)
                .job(job)
                .coverLetter("Tailored cover letter")
                .build();

        ApplicationResult result = adapter.execute(context);
        assertThat(result).isNotNull();
        assertThat(result.getApplicationId()).isEqualTo(appId);
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
    }

    @Test
    @DisplayName("AIProvider abstraction should accept AIRequest and return AIResponse")
    void testAIProviderAbstraction() {
        AIProvider provider = new StandardStructuredAiProvider();

        AIRequest request = AIRequest.builder()
                .taskType("GENERATE_COVER_LETTER")
                .prompt("Senior Full Stack Java Engineer at Stripe")
                .build();

        AIResponse response = provider.generate(request);
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNotBlank();
        assertThat(response.getModelName()).isEqualTo("STANDARD_STRUCTURED_AI_PROVIDER");
        assertThat(response.getTokensUsed()).isPositive();
    }

    @Test
    @DisplayName("FileStorageService abstraction should store, get, and delete files")
    void testFileStorageServiceAbstraction() {
        FileStorageService storage = new LocalStorageService();
        UUID userId = UUID.randomUUID();

        byte[] payload = "Candidate Resume Content".getBytes(StandardCharsets.UTF_8);
        FileUpload upload = FileUpload.builder()
                .fileName("candidate_cv.pdf")
                .contentType("application/pdf")
                .size(payload.length)
                .bytes(payload)
                .userId(userId)
                .build();

        StoredFile stored = storage.store(upload);
        assertThat(stored).isNotNull();
        assertThat(stored.getFileId()).isNotBlank();

        StoredFile retrieved = storage.get(stored.getFileId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getContent()).isEqualTo(payload);

        storage.delete(stored.getFileId());
    }
}
