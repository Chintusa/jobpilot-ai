# Spring Boot Backend Architecture & Technical Implementation Plan

## 1. Objective
Design and implement the production-grade **Spring Boot 3.x** modular monolith backend for **JobPilot AI** with Java 21, Maven, PostgreSQL, Redis, Flyway migrations, Spring Security (JWT), clean DTO-entity separation, service layers, repositories, REST controllers, validation, and automated tests.

---

## 2. System Modules & Package Architecture

The backend will be structured under `com.jobpilot`:

```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/jobpilot/
│   │   │   ├── JobPilotApplication.java
│   │   │   ├── common/                  # BaseEntity, ApiResponse, ErrorResponse, GlobalExceptionHandler, SecurityUtils
│   │   │   ├── config/                  # SecurityConfig, RedisConfig, OpenApiConfig, CorsConfig
│   │   │   ├── security/                # JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal, UserDetailsServiceImpl
│   │   │   ├── auth/                    # AuthController, AuthService, DTOs (LoginRequest, RegisterRequest, AuthResponse, TokenRefreshRequest)
│   │   │   ├── user/                    # User entity, UserRepository, UserService, UserController, UserDTOs
│   │   │   ├── candidate/               # CandidateProfile, Experience, Education, Skill, Repository, Service, Controller
│   │   │   ├── resume/                  # Resume entity, ResumeRepository, ResumeService, ResumeController
│   │   │   ├── preferences/             # JobPreferences entity, JobPreferencesRepository, JobPreferencesService, Controller
│   │   │   ├── jobs/                    # Job entity, JobSource, JobSourceRepository, JobRepository, JobService, JobController
│   │   │   ├── matching/                # JobMatch entity, MatchBreakdown, MatchingEngineService, MatchingController
│   │   │   ├── ai/                      # AiProvider interface, OpenAiProvider, GeminiProvider, AnthropicProvider, AiService
│   │   │   ├── applications/            # Application, ApplicationEvent, ScreeningQuestion, ScreeningAnswer, ApplicationService, Controller
│   │   │   ├── automation/              # HumanIntervention, AgentSchedule, SearchRun, AutomationService, Controller
│   │   │   └── notifications/           # Notification entity, NotificationRepository, NotificationService, Controller
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   │           ├── V1__create_initial_schema.sql
│   │           └── V2__seed_demo_data.sql
│   └── test/
│       ├── java/com/jobpilot/
│       │   ├── JobPilotApplicationTests.java
│       │   ├── auth/AuthControllerTest.java
│       │   ├── jobs/JobServiceTest.java
│       │   └── matching/MatchingEngineServiceTest.java
│       └── resources/
│           └── application-test.yml
└── infrastructure/
    └── docker-compose.yml              # PostgreSQL 16 + Redis 7 + pgAdmin
```

---

## 3. Core Database Schema & Entities

The relational database schema will be managed via **Flyway**:

1. **`users`**: UUID PK, email, password_hash, name, phone, role (`ROLE_USER`, `ROLE_ADMIN`), enabled, created_at, updated_at.
2. **`resumes`**: UUID PK, user_id (FK), file_name, file_url, file_size, content_type, parsed_text, ats_score, is_active, created_at.
3. **`candidate_profiles`**: UUID PK, user_id (FK, unique), current_title, summary, total_experience_years, target_salary_min, target_salary_max, location, work_mode, completeness_score, created_at, updated_at.
4. **`profile_skills`**: UUID PK, profile_id (FK), name, category, proficiency, evidence_type (`DEMONSTRATED`, `MENTIONED`, `INFERRED`).
5. **`job_preferences`**: UUID PK, user_id (FK, unique), target_roles (JSON/array), locations (JSON/array), work_modes (JSON/array), min_salary, excluded_companies (JSON/array), auto_apply_enabled, auto_apply_min_score, auto_apply_daily_limit, require_approval.
6. **`job_sources`**: UUID PK, name (`LINKEDIN`, `INDEED`, `NAUKRI`, `INTERNAL`), enabled, adapter_class, last_sync_at.
7. **`jobs`**: UUID PK, source_id (FK), external_id, title, company, location, work_mode, salary_min, salary_max, salary_currency, salary_display, experience_min, experience_max, description, required_skills (JSON), preferred_skills (JSON), job_url, status, posted_at, created_at.
8. **`job_matches`**: UUID PK, user_id (FK), job_id (FK), overall_score, recommendation (`APPLY`, `REVIEW`, `SKIP`), breakdown (JSON), reasoning, status, created_at.
9. **`applications`**: UUID PK, user_id (FK), job_id (FK), resume_id (FK), status (`MATCHED`, `PREPARING`, `PENDING_REVIEW`, `SUBMITTED`, `INTERVIEW`, `OFFER`, `REJECTED`), tailored_resume_url, cover_letter, submission_method (`AI_AGENT`, `MANUAL`), applied_at, created_at, updated_at.
10. **`application_events`**: UUID PK, application_id (FK), event_type, message, metadata (JSON), created_at.
11. **`screening_questions`**: UUID PK, application_id (FK), question, ai_answer, candidate_answer, confidence (`HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`), source, status (`PENDING`, `ACCEPTED`, `EDITED`, `NEEDS_ANSWER`), created_at.
12. **`human_interventions`**: UUID PK, application_id (FK), user_id (FK), type (`CAPTCHA`, `MFA`, `UNKNOWN_QUESTION`, `LEGAL`, `MISSING_INFO`), description, required_input_type, status (`PENDING`, `RESOLVED`, `SKIPPED`), resolution_payload (JSON), created_at, resolved_at.
13. **`notifications`**: UUID PK, user_id (FK), title, message, type (`INFO`, `WARNING`, `SUCCESS`, `INTERVENTION`), is_read, action_url, created_at.
14. **`agent_schedules`**: UUID PK, user_id (FK), cron_expression, enabled, last_run_at, next_run_at, status (`IDLE`, `RUNNING`, `PAUSED`).

---

## 4. API Endpoints Plan

### Authentication & User
- `POST /api/v1/auth/register` — Register new user
- `POST /api/v1/auth/login` — Login & receive JWT access + refresh token
- `POST /api/v1/auth/refresh` — Rotate refresh token & issue new JWT
- `GET /api/v1/auth/me` — Fetch authenticated candidate profile & settings

### Candidate Profile & Resume
- `GET /api/v1/profile` & `PUT /api/v1/profile` — Fetch & update candidate profile
- `POST /api/v1/resumes/upload` — Upload resume (Multipart) & trigger parsing
- `GET /api/v1/resumes` & `PUT /api/v1/resumes/{id}/activate` — Manage resumes

### Job Discovery & Matching
- `GET /api/v1/jobs` — Filtered & paginated job listings with search criteria
- `GET /api/v1/jobs/{id}` — Full job details with AI recruiter assessment
- `GET /api/v1/jobs/{id}/match` — Calculate and return granular match breakdown
- `POST /api/v1/jobs/{id}/save` & `DELETE /api/v1/jobs/{id}/save` — Bookmark jobs

### Applications & Agent
- `GET /api/v1/applications` — List applications filtered by status
- `POST /api/v1/applications/prep/{jobId}` — Initialize application preparation & screening questions
- `PUT /api/v1/applications/screening/{questionId}` — Accept/edit screening answer
- `POST /api/v1/applications/{id}/submit` — Review & submit application
- `GET /api/v1/agent/status` & `POST /api/v1/agent/toggle` — AI Agent Control Center state

### Interventions & System
- `GET /api/v1/interventions` — List pending human-in-the-loop interventions
- `POST /api/v1/interventions/{id}/resolve` — Solve CAPTCHA/MFA or confirm legal
- `GET /api/v1/health` & `GET /actuator/health` — System, PostgreSQL, and Redis health probes

---

## 5. Verification Plan
1. Validate `docker-compose.yml` for local development.
2. Compile and package backend with `mvn clean package -DskipTests`.
3. Run comprehensive unit and integration tests with `mvn test` (using H2/embedded Redis for isolated CI/CD testing).
4. Verify Flyway migrations, security filters, DTO validations, and Actuator health probe.
