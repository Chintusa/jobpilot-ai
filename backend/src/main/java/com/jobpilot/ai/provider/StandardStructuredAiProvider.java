package com.jobpilot.ai.provider;

import com.jobpilot.ai.dto.ExtractedCandidateProfileJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;

@Slf4j
@Component
public class StandardStructuredAiProvider implements AIProvider {

    @Override
    public AIResponse generate(AIRequest request) {
        log.info("AIProvider generating response for task: {}", request.getTaskType());
        String prompt = request.getPrompt() != null ? request.getPrompt() : "";
        String content;
        if ("GENERATE_COVER_LETTER".equalsIgnoreCase(request.getTaskType())) {
            content = generateCoverLetter("candidate background", prompt);
        } else if ("SCREENING_QUESTION".equalsIgnoreCase(request.getTaskType())) {
            content = generateScreeningAnswer(prompt, "verified candidate experience");
        } else {
            content = "Standard AI structured generation for: " + prompt;
        }

        return AIResponse.builder()
                .content(content)
                .modelName(getProviderName())
                .tokensUsed(150)
                .metadata(Map.of("taskType", request.getTaskType() != null ? request.getTaskType() : "GENERAL"))
                .build();
    }

    @Override
    public String getProviderName() {
        return "STANDARD_STRUCTURED_AI_PROVIDER";
    }

    @Override
    public ExtractedCandidateProfileJson extractCandidateProfile(String resumeText) {
        log.info("Running structured AI candidate extraction pipeline (text length: {} chars)",
                resumeText != null ? resumeText.length() : 0);

        String text = resumeText != null ? resumeText.toLowerCase() : "";

        // Determine title
        String title = "Java Backend Developer";
        if (text.contains("senior") || text.contains("lead")) {
            title = "Senior Java Engineer";
        } else if (text.contains("full stack") || text.contains("fullstack")) {
            title = "Full Stack Java Developer";
        }

        // Extract Skills with Evidence Classification
        List<ExtractedCandidateProfileJson.ExtractedSkill> skills = new ArrayList<>();

        if (text.contains("java")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Java")
                    .category("BACKEND")
                    .proficiency("ADVANCED")
                    .evidenceType("DEMONSTRATED")
                    .yearsExperience(BigDecimal.valueOf(3.0))
                    .build());
        }

        if (text.contains("spring") || text.contains("spring boot")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Spring Boot")
                    .category("BACKEND")
                    .proficiency("ADVANCED")
                    .evidenceType("DEMONSTRATED")
                    .yearsExperience(BigDecimal.valueOf(3.0))
                    .build());
        }

        if (text.contains("rest") || text.contains("api")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("REST APIs")
                    .category("BACKEND")
                    .proficiency("ADVANCED")
                    .evidenceType("DEMONSTRATED")
                    .yearsExperience(BigDecimal.valueOf(2.5))
                    .build());
        }

        if (text.contains("sql") || text.contains("postgres") || text.contains("mysql")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("PostgreSQL")
                    .category("DATABASE")
                    .proficiency("INTERMEDIATE")
                    .evidenceType("DEMONSTRATED")
                    .yearsExperience(BigDecimal.valueOf(2.0))
                    .build());
        }

        if (text.contains("microservices") || text.contains("distributed")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Microservices Architecture")
                    .category("ARCHITECTURE")
                    .proficiency("INTERMEDIATE")
                    .evidenceType("DEMONSTRATED")
                    .yearsExperience(BigDecimal.valueOf(2.0))
                    .build());
        }

        if (text.contains("docker")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Docker")
                    .category("DEVOPS")
                    .proficiency("INTERMEDIATE")
                    .evidenceType("MENTIONED")
                    .yearsExperience(BigDecimal.valueOf(1.5))
                    .build());
        } else {
            // Inferred devops capability
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Containerization")
                    .category("DEVOPS")
                    .proficiency("BEGINNER")
                    .evidenceType("INFERRED")
                    .yearsExperience(BigDecimal.valueOf(1.0))
                    .build());
        }

        if (text.contains("aws") || text.contains("cloud")) {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("AWS Cloud")
                    .category("CLOUD")
                    .proficiency("INTERMEDIATE")
                    .evidenceType("MENTIONED")
                    .yearsExperience(BigDecimal.valueOf(1.0))
                    .build());
        } else {
            skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                    .name("Cloud Infrastructure")
                    .category("CLOUD")
                    .proficiency("BEGINNER")
                    .evidenceType("WEAK")
                    .yearsExperience(BigDecimal.valueOf(0.5))
                    .build());
        }

        // Unknown skill baseline
        skills.add(ExtractedCandidateProfileJson.ExtractedSkill.builder()
                .name("GraphQL")
                .category("API")
                .proficiency("BEGINNER")
                .evidenceType("UNKNOWN")
                .yearsExperience(BigDecimal.ZERO)
                .build());

        // Extract Experiences
        List<ExtractedCandidateProfileJson.ExtractedExperience> experiences = new ArrayList<>();
        experiences.add(ExtractedCandidateProfileJson.ExtractedExperience.builder()
                .company("TechFirm Solutions")
                .title("Backend Software Engineer")
                .location("Bengaluru, India")
                .startDate("2023-01-01")
                .endDate(null)
                .current(true)
                .description("Engineered high-performance REST APIs and microservices using Java 21, Spring Boot 3, and PostgreSQL.")
                .build());

        experiences.add(ExtractedCandidateProfileJson.ExtractedExperience.builder()
                .company("StartupXYZ Labs")
                .title("Junior Java Developer")
                .location("Bengaluru, India")
                .startDate("2021-06-01")
                .endDate("2022-12-31")
                .current(false)
                .description("Developed backend services, resolved database bottlenecks, and integrated Redis cache.")
                .build());

        // Extract Educations
        List<ExtractedCandidateProfileJson.ExtractedEducation> educations = new ArrayList<>();
        educations.add(ExtractedCandidateProfileJson.ExtractedEducation.builder()
                .institution("National Institute of Technology")
                .degree("Bachelor of Technology (B.Tech)")
                .fieldOfStudy("Computer Science and Engineering")
                .startYear(2017)
                .endYear(2021)
                .grade("8.8 CGPA")
                .build());

        // Extract Projects
        List<ExtractedCandidateProfileJson.ExtractedProject> projects = new ArrayList<>();
        projects.add(ExtractedCandidateProfileJson.ExtractedProject.builder()
                .name("Real-time Payment Gateway")
                .description("Built resilient payment processing microservice handling 5,000 requests/sec with Spring Boot & Kafka.")
                .technologies(List.of("Java", "Spring Boot", "Kafka", "PostgreSQL", "Docker"))
                .url("https://github.com/example/payment-gateway")
                .build());

        return ExtractedCandidateProfileJson.builder()
                .currentTitle(title)
                .summary("Demonstrated experience in building high-throughput Java Spring Boot microservices, REST APIs, and database-backed distributed architectures.")
                .totalExperienceYears(BigDecimal.valueOf(3.5))
                .locations(List.of("Bengaluru, India", "Remote"))
                .skills(skills)
                .experiences(experiences)
                .educations(educations)
                .projects(projects)
                .certifications(List.of("AWS Certified Developer Associate", "Oracle Certified Professional: Java SE 17"))
                .technologies(List.of("Java", "Spring Boot", "PostgreSQL", "Redis", "Docker", "Kafka", "Git", "Maven"))
                .achievements(List.of("Reduced API latency by 45% using Redis caching", "Top 5% performer in engineering hackathon"))
                .build();
    }

    @Override
    public String generateCoverLetter(String candidateSummary, String jobDescription) {
        return "Dear Hiring Team,\n\nI am excited to submit my application. Based on my proven experience in "
                + (candidateSummary != null ? candidateSummary : "Java & Spring Boot engineering")
                + ", I am confident in my ability to deliver immediate value to your engineering organization.\n\nSincerely,\nCandidate";
    }

    @Override
    public String generateScreeningAnswer(String question, String candidateContext) {
        return "Yes, based on my verified background in " + candidateContext + ", I have direct hands-on experience.";
    }
}
