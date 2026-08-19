package com.jobpilot.jobs.source;

import java.util.List;

public interface JobSource {

    String getSourceName();

    List<ExternalJob> search(JobSearchCriteria criteria);
}
