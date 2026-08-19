-- =============================================================================
-- JobPilot AI Seed Demo Data
-- =============================================================================

-- Seed default demo user (Password: password123 -> BCrypt hash)
INSERT INTO users (id, email, password_hash, name, phone, role, enabled)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'jhasaketan@example.com',
    '$2a$10$e8wVdI6y3lVv7P9V0E9RpuY5g7tYpS4u1D0R1b/J8lW1z6Z5K6O0G', -- BCrypt for 'password123'
    'Jhasaketan M.',
    '+91 98765 43210',
    'ROLE_USER',
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Seed Candidate Profile
INSERT INTO candidate_profiles (id, user_id, current_title, summary, total_experience_years, target_salary_min, target_salary_max, location, preferred_work_mode, completeness_score)
VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Java Backend Developer',
    'Enthusiastic Java developer with hands-on experience in Spring Boot and microservices architecture. Passionate about scalable backend systems and automated AI pipelines.',
    2.5,
    600000.00,
    1200000.00,
    'Bengaluru, India',
    'HYBRID',
    94
) ON CONFLICT (user_id) DO NOTHING;

-- Seed Profile Skills
INSERT INTO profile_skills (profile_id, name, category, proficiency, evidence_type, years_experience)
VALUES 
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Java', 'BACKEND', 'ADVANCED', 'DEMONSTRATED', 2.5),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Spring Boot', 'BACKEND', 'ADVANCED', 'DEMONSTRATED', 2.0),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'REST APIs', 'BACKEND', 'ADVANCED', 'DEMONSTRATED', 2.5),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'SQL', 'DATABASE', 'INTERMEDIATE', 'DEMONSTRATED', 2.0),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Microservices', 'ARCHITECTURE', 'INTERMEDIATE', 'DEMONSTRATED', 1.5);

-- Seed Job Preferences
INSERT INTO job_preferences (user_id, target_roles, locations, work_modes, min_salary, auto_apply_enabled, auto_apply_min_score, auto_apply_daily_limit, require_approval)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    '["Java Backend Developer", "Spring Boot Engineer", "Backend Developer"]',
    '["Bengaluru", "Hyderabad", "Remote"]',
    '["HYBRID", "REMOTE"]',
    600000.00,
    TRUE,
    85,
    5,
    TRUE
) ON CONFLICT (user_id) DO NOTHING;

-- Seed Job Source
INSERT INTO job_sources (id, name, enabled, adapter_class)
VALUES (
    'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
    'LINKEDIN',
    TRUE,
    'com.jobpilot.jobs.adapter.LinkedInJobSourceAdapter'
) ON CONFLICT (name) DO NOTHING;

-- Seed Sample Jobs
INSERT INTO jobs (id, source_id, external_id, title, company, location, work_mode, salary_min, salary_max, salary_currency, salary_display, experience_min, experience_max, description, required_skills, preferred_skills, job_url, status)
VALUES 
(
    'd4e5f6a7-b89c-0d1e-2f3a-4b5c6d7e8f9a',
    'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
    'tech-nova-java-101',
    'Java Backend Developer',
    'TechNova Technologies',
    'Bengaluru, India',
    'HYBRID',
    600000.00,
    900000.00,
    'INR',
    '₹6–9 LPA',
    0.0,
    2.0,
    'We are seeking a proactive Java Backend Developer to build and maintain high-scale Spring Boot REST APIs and distributed microservices.',
    '["Java", "Spring Boot", "REST APIs", "SQL"]',
    '["Docker", "PostgreSQL", "AWS"]',
    'https://example.com/jobs/technova-101',
    'ACTIVE'
),
(
    'e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b',
    'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
    'cloud-scale-spring-102',
    'Spring Boot Microservices Engineer',
    'CloudScale Systems',
    'Remote, India',
    'REMOTE',
    800000.00,
    1200000.00,
    'INR',
    '₹8–12 LPA',
    2.0,
    4.0,
    'CloudScale is hiring a Spring Boot Microservices Engineer to develop cloud-native distributed backend services.',
    '["Java", "Spring Boot", "Microservices", "PostgreSQL"]',
    '["Kubernetes", "Redis", "Kafka"]',
    'https://example.com/jobs/cloudscale-102',
    'ACTIVE'
) ON CONFLICT DO NOTHING;

-- Seed Job Match for TechNova
INSERT INTO job_matches (user_id, job_id, overall_score, recommendation, score_breakdown, reasoning, status)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'd4e5f6a7-b89c-0d1e-2f3a-4b5c6d7e8f9a',
    91,
    'APPLY',
    '{"technicalSkills": 94, "location": 100, "experience": 90, "projects": 88, "education": 100, "roleFit": 92}',
    'Your Java and Spring Boot experience directly fulfill 100% of core backend requirements with strong alignment to TechNova stack.',
    'VIEWED'
) ON CONFLICT DO NOTHING;
