-- =============================================================================
-- JobPilot AI Initial Schema Migration
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    parsed_text TEXT,
    ats_score INTEGER DEFAULT 90,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Candidate Profiles Table
CREATE TABLE IF NOT EXISTS candidate_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    current_title VARCHAR(255),
    summary TEXT,
    total_experience_years NUMERIC(4,1) DEFAULT 0.0,
    target_salary_min NUMERIC(12,2),
    target_salary_max NUMERIC(12,2),
    location VARCHAR(255),
    preferred_work_mode VARCHAR(50) DEFAULT 'HYBRID',
    completeness_score INTEGER DEFAULT 0,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    projects_json TEXT,
    certifications_json TEXT,
    achievements_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Candidate Skills Table
CREATE TABLE IF NOT EXISTS profile_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) DEFAULT 'TECHNICAL',
    proficiency VARCHAR(50) DEFAULT 'INTERMEDIATE',
    evidence_type VARCHAR(50) DEFAULT 'DEMONSTRATED',
    years_experience NUMERIC(3,1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Candidate Experience Table
CREATE TABLE IF NOT EXISTS profile_experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Candidate Education Table
CREATE TABLE IF NOT EXISTS profile_educations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES candidate_profiles(id) ON DELETE CASCADE,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(255) NOT NULL,
    field_of_study VARCHAR(255),
    start_year INTEGER,
    end_year INTEGER,
    grade VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Job Preferences Table
CREATE TABLE IF NOT EXISTS job_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    target_roles TEXT DEFAULT '[]', -- JSON Array string
    role_variations TEXT DEFAULT '[]',
    locations TEXT DEFAULT '[]',
    work_modes TEXT DEFAULT '[]',
    min_salary NUMERIC(12,2) DEFAULT 0,
    max_salary NUMERIC(12,2),
    min_experience NUMERIC(3,1) DEFAULT 0.0,
    max_experience NUMERIC(3,1),
    industries TEXT DEFAULT '[]',
    required_skills TEXT DEFAULT '[]',
    preferred_skills TEXT DEFAULT '[]',
    excluded_companies TEXT DEFAULT '[]',
    excluded_keywords TEXT DEFAULT '[]',
    job_types TEXT DEFAULT '[]',
    auto_apply_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_apply_min_score INTEGER NOT NULL DEFAULT 85,
    auto_apply_daily_limit INTEGER NOT NULL DEFAULT 5,
    require_approval BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. Job Sources Table
CREATE TABLE IF NOT EXISTS job_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    adapter_class VARCHAR(255) NOT NULL,
    last_sync_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Jobs Table
CREATE TABLE IF NOT EXISTS jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID REFERENCES job_sources(id) ON DELETE SET NULL,
    external_id VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    work_mode VARCHAR(50) NOT NULL DEFAULT 'HYBRID',
    salary_min NUMERIC(12,2),
    salary_max NUMERIC(12,2),
    salary_currency VARCHAR(10) DEFAULT 'INR',
    salary_display VARCHAR(100),
    experience_min NUMERIC(4,1) DEFAULT 0.0,
    experience_max NUMERIC(4,1),
    description TEXT NOT NULL,
    required_skills TEXT DEFAULT '[]', -- JSON Array string
    preferred_skills TEXT DEFAULT '[]',
    job_url VARCHAR(1024),
    canonical_url VARCHAR(1024),
    dedup_hash VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    posted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_source_external UNIQUE (source_id, external_id)
);

-- 10. Job Matches Table
CREATE TABLE IF NOT EXISTS job_matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    overall_score INTEGER NOT NULL,
    classification VARCHAR(50) NOT NULL DEFAULT 'GOOD', -- EXCELLENT, STRONG, GOOD, POSSIBLE, LOW
    recommendation VARCHAR(50) NOT NULL DEFAULT 'REVIEW', -- APPLY, REVIEW, SKIP
    score_breakdown TEXT DEFAULT '{}', -- JSON Object
    reasoning TEXT,
    status VARCHAR(50) DEFAULT 'UNVIEWED', -- UNVIEWED, VIEWED, SAVED, DISMISSED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_job_match UNIQUE (user_id, job_id)
);

-- 11. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    resume_id UUID REFERENCES resumes(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'MATCHED', -- MATCHED, PREPARING, PENDING_REVIEW, SUBMITTED, INTERVIEW, OFFER, REJECTED
    preparation_state VARCHAR(50) NOT NULL DEFAULT 'PREPARING', -- PREPARING, READY_FOR_REVIEW, REQUIRES_USER_INPUT, USER_APPROVED, SUBMITTED
    application_summary TEXT,
    missing_information TEXT DEFAULT '[]', -- JSON Array of missing items
    tailored_resume_content TEXT,
    tailored_resume_url VARCHAR(1024),
    cover_letter TEXT,
    submission_method VARCHAR(50) DEFAULT 'AI_AGENT', -- AI_AGENT, MANUAL
    applied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. Application Events Table (Audit log)
CREATE TABLE IF NOT EXISTS application_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 13. Screening Questions Table
CREATE TABLE IF NOT EXISTS screening_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    ai_answer TEXT,
    candidate_answer TEXT,
    confidence VARCHAR(50) DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW, UNKNOWN
    source VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, ACCEPTED, EDITED, NEEDS_ANSWER
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. Human Interventions Table
CREATE TABLE IF NOT EXISTS human_interventions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES applications(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(50) NOT NULL, -- CAPTCHA, MFA, UNKNOWN_QUESTION, LEGAL_DECLARATION, MISSING_INFO, UNSUPPORTED_FLOW, ACCESS_CONTROL
    type VARCHAR(50), -- Backward compatibility alias for reason
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, RESOLVED, CANCELLED
    required_input VARCHAR(100) DEFAULT 'TEXT',
    required_input_type VARCHAR(50) DEFAULT 'TEXT',
    context TEXT DEFAULT '{}', -- JSON metadata, screenshots, employer portal context
    resolution_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

-- 15. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 16. Agent Schedules Table
CREATE TABLE IF NOT EXISTS agent_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    cron_expression VARCHAR(100) NOT NULL DEFAULT '0 0/30 * * * ?',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'IDLE',
    last_run_at TIMESTAMP WITH TIME ZONE,
    next_run_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 17. Search Runs Table (Agent Execution History & Audit Trail)
CREATE TABLE IF NOT EXISTS search_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED', -- IN_PROGRESS, COMPLETED, FAILED
    search_strategies TEXT DEFAULT '[]', -- JSON Array of queries
    role_variations TEXT DEFAULT '[]', -- JSON Array of role variants
    sources_queried TEXT DEFAULT '[]', -- JSON Array of sources
    query VARCHAR(255),
    number_found INTEGER NOT NULL DEFAULT 0,
    duplicates_removed INTEGER NOT NULL DEFAULT 0,
    filtered_jobs INTEGER NOT NULL DEFAULT 0,
    matched_jobs INTEGER NOT NULL DEFAULT 0,
    recommended_jobs INTEGER NOT NULL DEFAULT 0,
    errors TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    audit_log TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices for high performance
CREATE INDEX IF NOT EXISTS idx_jobs_title ON jobs(title);
CREATE INDEX IF NOT EXISTS idx_jobs_company ON jobs(company);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_job_matches_user ON job_matches(user_id);
CREATE INDEX IF NOT EXISTS idx_job_matches_score ON job_matches(overall_score DESC);
CREATE INDEX IF NOT EXISTS idx_applications_user ON applications(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);
CREATE INDEX IF NOT EXISTS idx_interventions_user_status ON human_interventions(user_id, status);
CREATE INDEX IF NOT EXISTS idx_search_runs_user ON search_runs(user_id, started_at DESC);
