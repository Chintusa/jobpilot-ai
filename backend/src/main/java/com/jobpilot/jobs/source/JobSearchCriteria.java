package com.jobpilot.jobs.source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchCriteria {

    private String keyword;
    private List<String> targetRoles;
    private List<String> locations;
    private List<String> workModes;
    private BigDecimal minSalary;
    private BigDecimal maxExperience;
    @Builder.Default
    private int limit = 20;
}
