# JobPilot AI — Architecture & Implementation Lifecycle Stages

```text
STAGE A — DESIGN
│
├── Analyze 7 designs
├── Create design system
└── Create frontend architecture
        ↓
STAGE B — FRONTEND FOUNDATION
│
├── React/Vite
├── Routing
├── Design system
├── Sidebar
├── Navbar
└── Shared components
        ↓
STAGE C — CORE BACKEND
│
├── Spring Boot
├── PostgreSQL
├── Redis
├── Authentication
└── User management
        ↓
STAGE D — CANDIDATE INTELLIGENCE
│
├── Resume upload
├── PDF/DOCX parsing
├── AI extraction
└── Candidate profile
        ↓
STAGE E — JOB INTELLIGENCE
│
├── Job sources
├── Normalization
├── Deduplication
├── AI matching
└── Recruiter scoring
        ↓
STAGE F — APPLICATION AGENT
│
├── Application preparation
├── Cover letters
├── Screening questions
└── Human review
        ↓
STAGE G — AUTOMATION
│
├── Playwright
├── Application adapters
├── Human intervention
└── Controlled auto-apply
        ↓
STAGE H — PRODUCTION
│
├── Tracking
├── Notifications
├── Scheduling
├── Security
├── Testing
└── Deployment
```

---

## Stage Breakdown & Completion Status

### ✅ STAGE A — DESIGN
- **Analyze 7 designs**: Analyzed `01-dashboard.png` through `07-application-tracker.png` from `/design-reference/`.
- **Create design system**: Authored in [`docs/DESIGN_SYSTEM.md`](file:///c:/Users/jhask/Downloads/jobpilot-ai/docs/DESIGN_SYSTEM.md).
- **Create frontend architecture**: Authored in [`docs/FRONTEND_ARCHITECTURE.md`](file:///c:/Users/jhask/Downloads/jobpilot-ai/docs/FRONTEND_ARCHITECTURE.md) and [`docs/COMPONENT_ARCHITECTURE.md`](file:///c:/Users/jhask/Downloads/jobpilot-ai/docs/COMPONENT_ARCHITECTURE.md).

### ✅ STAGE B — FRONTEND FOUNDATION
- **React/Vite**: Production-ready React 18 + TypeScript + Tailwind CSS configured.
- **Routing**: 13 responsive routes registered in `frontend/src/routes/`.
- **Sidebar & Navbar**: Collapsible 240px $\leftrightarrow$ 64px desktop sidebar + mobile drawer in `AppShell.tsx` and `TopNav.tsx`.
- **Shared Components**: Full library of atoms (`Button`, `Card`, `Badge`, `Input`, `ProgressBar`, `Slider`, `Toggle`) and AI signature components (`AIOrb`, `AIAgentStatus`, `MatchScoreRing`, `ScreeningQuestionCard`).

### ✅ STAGE C — CORE BACKEND
- **Spring Boot**: Java 21 + Spring Boot 3.3.3 modular monolith under `com.jobpilot`.
- **PostgreSQL**: PostgreSQL 16 connection with Hikari pool and Flyway schema migrations (`V1` & `V2`).
- **Redis**: Redis 7 connection with `RedisTemplate` JSON serialization.
- **Authentication**: Spring Security 6, BCrypt, JJWT 0.12.6, and refresh token rotation (`/api/v1/auth/*`).
- **User Management**: `User` entity, `UserRepository`, `UserDto`, and `/api/v1/auth/me`.

### ✅ STAGE D — CANDIDATE INTELLIGENCE
- **Resume Upload**: Multipart file handling (`PDF`, `DOC`, `DOCX`) via `POST /api/resumes`.
- **PDF/DOCX Parsing**: Document parsing engine powered by Apache Tika (`tika-core` 2.9.2).
- **AI Extraction**: `ResumeAiExtractionService` generating classified skills (`DEMONSTRATED`, `MENTIONED`, `INFERRED`), work experience, and degrees via `POST /api/resumes/{id}/process`.
- **Candidate Profile**: `CandidateProfile` entity, completeness scoring, and edit/approval via `GET /api/candidate-profile` & `PUT /api/candidate-profile`.

### ✅ STAGE E — JOB INTELLIGENCE
- **Job Sources**: Modular `JobSource` interface and `MockJobSource` implementation.
- **Normalization**: `JobNormalizer` standardizing titles, locations, work modes, and skills JSON.
- **Deduplication**: Source ID + external ID unique constraints and deduplication checks in PostgreSQL.
- **AI Matching & Recruiter Scoring**: `MatchingEngineService` evaluating 8 weighted recruiter categories (91% Strong Match).

### ✅ STAGE F — APPLICATION AGENT
- **Application Preparation**: `ApplicationService.prepareApplication` assembling tailored assets.
- **Cover Letters**: Auto-tailored cover letter generation per employer requirements.
- **Screening Questions**: `ScreeningQuestion` model with confidence levels (`HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`) and inline accept/edit dialogs.
- **Human Review**: Mandatory human-in-the-loop review modal prior to submission confirmation.

### ✅ STAGE G — AUTOMATION
- **Playwright**: Independent `application-worker/` browser automation service setup.
- **Application Adapters**: Modular website application adapter interface (`ApplicationAdapter`).
- **Human Intervention**: `HumanIntervention` model with resolution workflows for CAPTCHA, MFA, and legal declarations (`/api/v1/interventions/*`).
- **Controlled Auto-Apply**: Daily application limit stepper and policy enforcement (`require_approval = true`).

### ✅ STAGE H — PRODUCTION
- **Tracking**: Applications lifecycle state machine:
  ```text
  Main Path:
  DRAFT -> PREPARING -> READY_FOR_REVIEW -> APPROVED -> SUBMITTING -> SUBMITTED

  Alternative Paths:
  PREPARING -> REQUIRES_USER_INPUT -> PREPARING
  SUBMITTING -> FAILED
  SUBMITTED -> INTERVIEW -> OFFER
  ```
- **Notifications**: `Notification` entity, email provider abstraction, deduplication, and `/api/v1/notifications/*`.
- **Scheduling**: `AgentSchedule` entity and `@EnableScheduling` background runner.
- **Security**: Stateless JWT filter, password encryption, CORS configuration, rate limits, externalized secrets.
- **Testing**: Automated test suites passing in `mvn test` (63/63 backend tests, 12/12 worker tests, 5/5 frontend tests).
- **Deployment**: Multi-container `docker-compose.yml` for local and staging environments.
