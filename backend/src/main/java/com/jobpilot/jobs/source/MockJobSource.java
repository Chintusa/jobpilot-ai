package com.jobpilot.jobs.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MockJobSource implements JobSource {

    public static final String SOURCE_NAME = "MOCK_DISCOVERY_ENGINE";

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<ExternalJob> search(JobSearchCriteria criteria) {
        log.info("Searching jobs via MockJobSource with criteria: {}", criteria);

        List<ExternalJob> pool = createMockPool();

        return pool.stream()
                .filter(job -> {
                    if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                        String kw = criteria.getKeyword().toLowerCase();
                        String[] tokens = kw.split("\\s+");
                        String target = (job.getRawTitle() + " " + job.getRawCompany() + " " + job.getRawDescription()).toLowerCase();
                        boolean match = Arrays.stream(tokens).anyMatch(target::contains);
                        if (!match) return false;
                    }
                    if (criteria.getLocations() != null && !criteria.getLocations().isEmpty()) {
                        boolean matchLoc = criteria.getLocations().stream()
                                .anyMatch(l -> job.getRawLocation().toLowerCase().contains(l.toLowerCase()));
                        if (!matchLoc) return false;
                    }
                    if (criteria.getWorkModes() != null && !criteria.getWorkModes().isEmpty()) {
                        boolean matchMode = criteria.getWorkModes().stream()
                                .anyMatch(m -> job.getRawWorkMode().equalsIgnoreCase(m));
                        if (!matchMode) return false;
                    }
                    return true;
                })
                .limit(criteria.getLimit() > 0 ? criteria.getLimit() : 20)
                .collect(Collectors.toList());
    }

    private List<ExternalJob> createMockPool() {
        List<ExternalJob> jobs = new ArrayList<>();

        jobs.add(ExternalJob.builder()
                .externalId("mock-technova-101")
                .sourceName(SOURCE_NAME)
                .rawTitle("Java Backend Developer")
                .rawCompany("TechNova Technologies")
                .rawLocation("Bengaluru, Karnataka, India")
                .rawWorkMode("Hybrid")
                .salaryMin(BigDecimal.valueOf(600000.00))
                .salaryMax(BigDecimal.valueOf(900000.00))
                .salaryCurrency("INR")
                .salaryDisplay("₹6–9 LPA")
                .experienceMin(BigDecimal.valueOf(0.0))
                .experienceMax(BigDecimal.valueOf(2.0))
                .rawDescription("We are looking for a Java Backend Developer to build scalable enterprise Spring Boot REST APIs and distributed microservices.")
                .rawSkills(List.of("Java", "Spring Boot", "REST APIs", "SQL", "Microservices"))
                .jobUrl("https://example.com/jobs/technova-101")
                .postedAt(Instant.now().minusSeconds(3600 * 4))
                .build());

        jobs.add(ExternalJob.builder()
                .externalId("mock-cloudscale-102")
                .sourceName(SOURCE_NAME)
                .rawTitle("Spring Boot Microservices Engineer")
                .rawCompany("CloudScale Systems")
                .rawLocation("Remote - India")
                .rawWorkMode("Remote")
                .salaryMin(BigDecimal.valueOf(800000.00))
                .salaryMax(BigDecimal.valueOf(1200000.00))
                .salaryCurrency("INR")
                .salaryDisplay("₹8–12 LPA")
                .experienceMin(BigDecimal.valueOf(2.0))
                .experienceMax(BigDecimal.valueOf(4.0))
                .rawDescription("Join CloudScale Systems to architect high-throughput microservices using Java 21, Spring Boot 3, Kafka, and PostgreSQL.")
                .rawSkills(List.of("Java", "Spring Boot", "Microservices", "PostgreSQL", "Kafka"))
                .jobUrl("https://example.com/jobs/cloudscale-102")
                .postedAt(Instant.now().minusSeconds(3600 * 12))
                .build());

        jobs.add(ExternalJob.builder()
                .externalId("mock-nextgen-103")
                .sourceName(SOURCE_NAME)
                .rawTitle("Senior Java Backend Architect")
                .rawCompany("NextGen AI Labs")
                .rawLocation("Bengaluru, India")
                .rawWorkMode("Hybrid")
                .salaryMin(BigDecimal.valueOf(1200000.00))
                .salaryMax(BigDecimal.valueOf(1800000.00))
                .salaryCurrency("INR")
                .salaryDisplay("₹12–18 LPA")
                .experienceMin(BigDecimal.valueOf(3.0))
                .experienceMax(BigDecimal.valueOf(6.0))
                .rawDescription("Lead backend engineering team building AI-driven workflow engines with Spring Boot, Redis cache clusters, and Docker.")
                .rawSkills(List.of("Java", "Spring Boot", "Redis", "Docker", "AWS", "REST APIs"))
                .jobUrl("https://example.com/jobs/nextgen-103")
                .postedAt(Instant.now().minusSeconds(3600 * 24))
                .build());

        jobs.add(ExternalJob.builder()
                .externalId("mock-fintech-104")
                .sourceName(SOURCE_NAME)
                .rawTitle("Full Stack Java & React Engineer")
                .rawCompany("FinPay Solutions")
                .rawLocation("Hyderabad, India")
                .rawWorkMode("Onsite")
                .salaryMin(BigDecimal.valueOf(700000.00))
                .salaryMax(BigDecimal.valueOf(1000000.00))
                .salaryCurrency("INR")
                .salaryDisplay("₹7–10 LPA")
                .experienceMin(BigDecimal.valueOf(1.0))
                .experienceMax(BigDecimal.valueOf(3.0))
                .rawDescription("Develop secure payment processing services and modern responsive merchant dashboards using Java, Spring, and React.")
                .rawSkills(List.of("Java", "Spring Boot", "React", "TypeScript", "SQL"))
                .jobUrl("https://example.com/jobs/finpay-104")
                .postedAt(Instant.now().minusSeconds(3600 * 48))
                .build());

        return jobs;
    }
}
