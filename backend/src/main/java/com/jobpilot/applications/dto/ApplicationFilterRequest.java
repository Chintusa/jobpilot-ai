package com.jobpilot.applications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFilterRequest {
    private String status;
    private String source;
    private String company;
    private String query;
    private Instant startDate;
    private Instant endDate;
    private String sortBy; // createdAt, appliedAt, company
    private String sortDirection; // ASC, DESC
}
