package com.jobpilot.jobs.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.entity.JobSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobNormalizer {

    private final ObjectMapper objectMapper;

    private static final Set<String> TRACKING_PARAMS = Set.of(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "ref", "referer", "referrer", "fbclid", "gclid", "source", "src", "tracking_id", "session_id"
    );

    public Job normalize(ExternalJob externalJob, JobSource sourceEntity) {
        String normalizedTitle = cleanString(externalJob.getRawTitle());
        String normalizedCompany = cleanString(externalJob.getRawCompany());
        String normalizedLocation = normalizeLocation(externalJob.getRawLocation());
        String normalizedWorkMode = normalizeWorkMode(externalJob.getRawWorkMode());
        String canonicalUrl = normalizeUrl(externalJob.getJobUrl());
        String dedupHash = computeDedupHash(normalizedCompany, normalizedTitle, normalizedLocation);

        List<String> requiredSkills = externalJob.getRawSkills() != null ? externalJob.getRawSkills() : new ArrayList<>();
        String requiredSkillsJson = toJson(requiredSkills);

        return Job.builder()
                .source(sourceEntity)
                .externalId(externalJob.getExternalId())
                .title(normalizedTitle)
                .company(normalizedCompany)
                .location(normalizedLocation)
                .workMode(normalizedWorkMode)
                .salaryMin(externalJob.getSalaryMin())
                .salaryMax(externalJob.getSalaryMax())
                .salaryCurrency(externalJob.getSalaryCurrency() != null ? externalJob.getSalaryCurrency() : "INR")
                .salaryDisplay(externalJob.getSalaryDisplay())
                .experienceMin(externalJob.getExperienceMin())
                .experienceMax(externalJob.getExperienceMax())
                .description(cleanString(externalJob.getRawDescription()))
                .requiredSkills(requiredSkillsJson)
                .preferredSkills("[\"Docker\", \"AWS\", \"Redis\"]")
                .jobUrl(externalJob.getJobUrl())
                .canonicalUrl(canonicalUrl)
                .dedupHash(dedupHash)
                .status("ACTIVE")
                .postedAt(externalJob.getPostedAt())
                .build();
    }

    public String normalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "https";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            String path = uri.getPath() != null ? uri.getPath().replaceAll("/+$", "") : "";

            // Filter out tracking query parameters
            String query = uri.getRawQuery();
            StringBuilder cleanQuery = new StringBuilder();
            if (query != null && !query.isBlank()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String key = (idx > 0) ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8).toLowerCase() : pair.toLowerCase();
                    if (!TRACKING_PARAMS.contains(key) && !key.startsWith("utm_")) {
                        if (cleanQuery.length() > 0) cleanQuery.append("&");
                        cleanQuery.append(pair);
                    }
                }
            }

            String result = scheme + "://" + host + path;
            if (cleanQuery.length() > 0) {
                result += "?" + cleanQuery.toString();
            }
            return result;
        } catch (Exception e) {
            return rawUrl.trim().replaceAll("\\?.*$", "");
        }
    }

    public String computeDedupHash(String company, String title, String location) {
        String input = (cleanForHash(company) + "|" + cleanForHash(title) + "|" + cleanForHash(location)).toLowerCase();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input.replaceAll("[^a-z0-9]", "");
        }
    }

    private String cleanForHash(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String cleanString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    private String normalizeLocation(String rawLocation) {
        if (rawLocation == null || rawLocation.isBlank()) return "Remote, India";
        String loc = rawLocation.trim();
        if (loc.toLowerCase().contains("bengaluru") || loc.toLowerCase().contains("bangalore")) {
            return "Bengaluru, India";
        }
        if (loc.toLowerCase().contains("hyderabad")) {
            return "Hyderabad, India";
        }
        if (loc.toLowerCase().contains("pune")) {
            return "Pune, India";
        }
        if (loc.toLowerCase().contains("remote")) {
            return "Remote, India";
        }
        return loc;
    }

    private String normalizeWorkMode(String rawMode) {
        if (rawMode == null) return "HYBRID";
        String m = rawMode.trim().toUpperCase();
        if (m.contains("REMOTE")) return "REMOTE";
        if (m.contains("ONSITE") || m.contains("ON-SITE") || m.contains("OFFICE")) return "ONSITE";
        return "HYBRID";
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("Could not serialize skills list to JSON: {}", e.getMessage());
            return "[]";
        }
    }
}
