package com.jobpilot.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ObservabilityTest {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private CorrelationIdFilter correlationIdFilter;

    @Test
    public void testMetricsServiceIncrements() {
        // Job Search Run Metric
        metricsService.incrementJobSearchRun("LinkedIn");
        Counter searchRuns = meterRegistry.find("jobpilot.jobsearch.runs").tag("source", "LinkedIn").counter();
        assertNotNull(searchRuns);
        assertTrue(searchRuns.count() >= 1);

        // AI Request Metric
        metricsService.incrementAiRequest("OpenAI", "resumeParse");
        Counter aiRequests = meterRegistry.find("jobpilot.ai.requests").tag("provider", "OpenAI").counter();
        assertNotNull(aiRequests);
        assertTrue(aiRequests.count() >= 1);

        // Application Execution Metric
        metricsService.incrementApplicationExecution("SUCCESS");
        Counter appExecs = meterRegistry.find("jobpilot.application.executions").tag("status", "SUCCESS").counter();
        assertNotNull(appExecs);
        assertTrue(appExecs.count() >= 1);
    }

    @Test
    public void testCorrelationIdFilterGeneratesId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // No correlation ID in request
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Filter should generate one and set it in response header
        String responseHeaderId = response.getHeader("X-Correlation-ID");
        assertNotNull(responseHeaderId);
        assertFalse(responseHeaderId.isBlank());
        
        // Ensure MDC is cleared after request
        assertNull(MDC.get("correlationId"));
    }

    @Test
    public void testCorrelationIdFilterUsesProvidedId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String existingId = "req-12345-abc";
        request.addHeader("X-Correlation-ID", existingId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        String responseHeaderId = response.getHeader("X-Correlation-ID");
        assertEquals(existingId, responseHeaderId);
        assertNull(MDC.get("correlationId"));
    }
}
