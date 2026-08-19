package com.jobpilot.jobs.service;

import com.jobpilot.common.exception.ResourceNotFoundException;
import com.jobpilot.jobs.dto.JobDto;
import com.jobpilot.jobs.entity.Job;
import com.jobpilot.jobs.repository.JobRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public Page<JobDto> searchJobs(String keyword, String location, String workMode, Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate companyLike = cb.like(cb.lower(root.get("company")), pattern);
                predicates.add(cb.or(titleLike, companyLike));
            }

            if (location != null && !location.isBlank()) {
                String pattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), pattern));
            }

            if (workMode != null && !workMode.isBlank() && !workMode.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(cb.upper(root.get("workMode")), workMode.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable)
                .map(job -> {
                    JobDto dto = JobDto.fromEntity(job);
                    dto.setMatchScore(91); // Default high-confidence recruiter rank for matching profile
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public JobDto getJobById(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opportunity not found with id: " + id));
        JobDto dto = JobDto.fromEntity(job);
        dto.setMatchScore(91);
        return dto;
    }
}
