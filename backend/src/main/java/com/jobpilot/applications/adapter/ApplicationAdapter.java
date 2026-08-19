package com.jobpilot.applications.adapter;

import com.jobpilot.jobs.entity.Job;

public interface ApplicationAdapter {

    boolean supports(Job job);

    ApplicationResult execute(ApplicationContext context);
}
