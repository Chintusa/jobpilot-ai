# JobPilot AI — Master Implementation Plan

## 1. Project Overview

**Product:** JobPilot AI

JobPilot AI is an AI-powered job search and application platform that helps a candidate move from resume to job discovery, matching, application preparation, human review, supported browser automation, and application tracking.

### Core Workflow

```text
Resume
  ↓
Candidate Profile
  ↓
Job Discovery
  ↓
Job Normalization & Deduplication
  ↓
Eligibility Filtering
  ↓
AI Matching
  ↓
Recruiter Scoring
  ↓
Job Ranking
  ↓
Application Preparation
  ↓
Human Review
  ↓
Supported Application Automation
  ↓
Application Tracking
```

### Agent Modules

1. Candidate Agent
2. Job Search Agent
3. Matching Agent
4. Application Preparation Agent
5. Application Agent
6. Application Tracking Agent

---

# 2. Product Principles

- The UI should follow the supplied design references.
- The application should feel like a premium AI SaaS product rather than a traditional job board.
- Never fabricate candidate information.
- Never bypass CAPTCHA.
- Never bypass MFA.
- Never bypass anti-bot or security mechanisms.
- Stop and request human intervention when a flow requires it.
- Final application submission should initially require explicit user approval.
- Job sources must be modular.
- Application flows must be modular.
- AI providers must be modular.
- All automated decisions should be auditable.
- Prefer a modular monolith initially; keep Playwright as a separate worker.
- Build incrementally and test every phase before moving forward.

---

# 3. Technology Stack

## Frontend

- React
- Vite
- TypeScript
- Tailwind CSS
- React Router
- TanStack Query
- Axios
- React Hook Form
- Zod

## Backend

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- PostgreSQL
- Redis
- Flyway
- Maven

## AI

Provider abstraction supporting:

- OpenAI-compatible providers
- Gemini-compatible providers
- Anthropic-compatible providers

## Resume Processing

- Apache PDFBox
- Apache POI

## Browser Automation

- Playwright
- Separate application-worker service

## Storage

- S3-compatible storage or Cloudinary

## Deployment

Initial target:

- Frontend: Vercel
- Backend: Render
- PostgreSQL: managed PostgreSQL
- Redis: managed Redis
- Application worker: separate deployable service

---

# 4. Repository Structure

```text
jobpilot-ai/
├── frontend/
├── backend/
├── application-worker/
├── shared/
├── infrastructure/
├── docs/
│   ├── MASTER_IMPLEMENTATION_PLAN.md
│   ├── IMPLEMENTATION_STATUS.md
│   ├── DESIGN_SYSTEM.md
│   ├── PAGE_INVENTORY.md
│   ├── COMPONENT_ARCHITECTURE.md
│   ├── FRONTEND_ARCHITECTURE.md
│   ├── IMPLEMENTATION_PLAN.md
│   └── PRODUCTION_READINESS.md
├── design-reference/
│   ├── 01-dashboard.png
│   ├── 02-landing-page.png
│   ├── 03-job-details.png
│   ├── 04-application-agent.png
│   ├── 05-agent-control-center.png
│   ├── 06-design-system.png
│   └── 07-application-tracker.png
└── README.md
```

Adjust filenames if the supplied design references use different names.

---

# 5. Development Strategy

Do NOT give every phase prompt to the coding agent at once.

The master plan defines the complete destination.

Each phase prompt defines the current implementation scope.

Recommended workflow:

```text
Read MASTER_IMPLEMENTATION_PLAN.md
        ↓
Read IMPLEMENTATION_STATUS.md
        ↓
Execute ONE phase
        ↓
Run tests/build
        ↓
Fix errors
        ↓
Update IMPLEMENTATION_STATUS.md
        ↓
Review architecture
        ↓
Proceed to next phase
```

Never skip ahead unless explicitly instructed.

---

# 6. Stage A — Design & Frontend Foundation

## Phase 0 — Design Analysis

### Objective

Analyze all seven visual references before implementing individual screens.

### Prompt

```text
You are the lead UI/UX engineer and frontend architect for JobPilot AI.

Analyze every image inside /design-reference/.

These images are the source of truth for the frontend visual design.

Do NOT implement individual pages yet.

Extract:
- color palette
- typography
- spacing
- borders
- radius
- shadows
- gradients
- cards
- buttons
- inputs
- navigation
- tables
- charts
- badges
- AI visual language
- 3D elements
- animation opportunities
- responsive behavior

Create:

/docs/DESIGN_SYSTEM.md
/docs/PAGE_INVENTORY.md
/docs/COMPONENT_ARCHITECTURE.md
/docs/FRONTEND_ARCHITECTURE.md
/docs/IMPLEMENTATION_PLAN.md

Map each design reference to a React page and define:
- route
- purpose
- sections
- reusable components
- data requirements
- user actions
- responsive behavior

Do not implement the pages.
Do not implement backend functionality.
Do not invent a different visual style.

After analysis, report:
1. Design system
2. Page inventory
3. Component inventory
4. Routing
5. Frontend architecture
6. Backend integration boundaries
7. Implementation order

Then stop.
```

### Exit Criteria

- Design system documented.
- Page inventory documented.
- Component architecture documented.
- Frontend architecture documented.
- Implementation plan documented.

---

## Phase 1 — Frontend Foundation

### Objective

Create the React/Vite application shell and reusable design system.

### Prompt

```text
Implement the JobPilot AI frontend foundation using:

- React
- Vite
- TypeScript
- Tailwind CSS
- React Router
- TanStack Query
- Axios
- React Hook Form
- Zod

Read:
- /docs/DESIGN_SYSTEM.md
- /docs/PAGE_INVENTORY.md
- /docs/COMPONENT_ARCHITECTURE.md
- /docs/FRONTEND_ARCHITECTURE.md
- /design-reference/

Implement:
- application shell
- responsive sidebar
- top navigation
- routing
- design tokens
- typography
- buttons
- cards
- inputs
- badges
- tabs
- modals
- loading states
- empty states
- error states
- AI visual components

Use mock data only.

Do not implement real backend functionality.

Run the application and fix build and console errors.
```

### Exit Criteria

- Frontend starts successfully.
- Routes exist.
- Layout is responsive.
- Reusable components exist.
- Design tokens are implemented.
- No blocking build/console errors.

---

## Phase 2 — Visual Screen Implementation

### Objective

Implement the seven designed screens individually.

### Prompt

```text
Implement the next JobPilot AI screen using its corresponding image in /design-reference/.

Read the design system and component architecture first.

Reproduce the reference accurately:
- layout
- spacing
- typography
- colors
- cards
- buttons
- charts
- badges
- AI elements
- 3D treatment
- responsive behavior

Use reusable components.
Use mock data.
Do not change unrelated screens.
Do not implement backend functionality.

Run the application and fix all errors before stopping.
```

Repeat this prompt one screen at a time for:

1. Landing Page
2. Dashboard
3. Job Details
4. Application Agent
5. Agent Control Center
6. Application Tracker
7. Any remaining supplied design screen

### Exit Criteria

All seven screens are implemented and visually aligned with the references.

---

# 7. Stage B — Core Backend

## Phase 3 — Repository & Backend Foundation

### Objective

Create the production backend foundation.

### Prompt

```text
Create the Spring Boot backend foundation for JobPilot AI.

Use:
- Java 21
- Spring Boot 3.x
- Maven
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway

Create modular packages for:
- auth
- user
- resume
- candidate
- preferences
- jobs
- matching
- applications
- notifications
- scheduler
- common

Add:
- configuration
- environment variables
- health endpoint
- PostgreSQL connection
- Redis connection
- Flyway
- global exception handling
- validation
- structured logging
- API response conventions

Add Docker Compose for local PostgreSQL and Redis if not already present.

Do not implement advanced business functionality yet.
Run tests and verify startup.
```

---

## Phase 4 — Authentication

### Objective

Implement secure user authentication.

### Requirements

- User entity
- Roles
- BCrypt
- JWT access tokens
- Refresh tokens
- Refresh token rotation
- Login
- Registration
- Logout
- Current-user endpoint
- Spring Security
- Protected endpoints
- Frontend auth state
- Protected routes

### Prompt

```text
Implement JobPilot AI authentication.

Backend:
- User entity
- roles
- BCrypt password hashing
- JWT access token
- refresh token
- refresh token rotation
- registration
- login
- logout
- /me
- Spring Security configuration
- authorization

Frontend:
- login
- register
- logout
- auth state
- protected routes
- API interceptor

Do not expose secrets in source code.

Write tests for authentication and authorization.
Run backend and frontend tests.
```

---

# 8. Stage C — Candidate Intelligence

## Phase 5 — Resume Intelligence

### Objective

Upload and parse candidate resumes.

### Requirements

Create:

- Resume entity
- Resume upload API
- File validation
- PDF parsing with PDFBox
- DOCX parsing with Apache POI
- Extracted text
- Resume metadata
- Secure storage abstraction

### Prompt

```text
Implement Resume Intelligence.

Create:
- Resume entity
- resume upload API
- secure file validation
- PDF parsing using Apache PDFBox
- DOCX parsing using Apache POI
- extracted resume text
- storage abstraction
- resume status

Validate:
- file type
- file size
- malformed files

Do not log resume contents.

Add tests for valid and invalid files.
```

---

## Phase 6 — Candidate Profile & AI Extraction

### Objective

Convert resume information into structured candidate data.

### Candidate Profile

Include:

- education
- experience
- skills
- projects
- certifications
- locations
- preferred roles
- industries
- technologies
- achievements

Skills should distinguish:

- demonstrated
- mentioned
- inferred
- weak
- unknown

### Prompt

```text
Implement Candidate Profile Intelligence.

Create CandidateProfile and related entities.

Build an AI provider abstraction.

The AI extraction process must return strict structured JSON.

Extract:
- education
- experience
- skills
- projects
- certifications
- technologies
- achievements
- locations

For skills classify evidence as:
- demonstrated
- mentioned
- inferred
- weak
- unknown

Never invent candidate information.

Allow the user to review and edit extracted information.

Store the final user-approved profile.

Add tests for parsing, validation, AI response validation, and profile updates.
```

---

## Phase 7 — Job Preferences

### Objective

Allow the candidate to define search criteria.

### Preferences

- target roles
- role variations
- locations
- remote/hybrid/on-site
- salary
- experience
- industries
- required skills
- preferred skills
- excluded companies
- excluded keywords
- job type
- auto-apply settings

### Prompt

```text
Implement Job Preferences.

Create entities, DTOs, validation, APIs and frontend forms for:

- target roles
- role variations
- locations
- work mode
- salary
- experience
- industries
- required skills
- preferred skills
- excluded companies
- excluded keywords
- job type
- auto-apply policy

Validate preferences.

Make the preferences available to the Job Search Agent and Matching Engine.
```

---

# 9. Stage D — Job Intelligence

## Phase 8 — Job Discovery

### Objective

Build modular job-source infrastructure.

### Interfaces

```java
interface JobSource {
    String getSourceName();
    List<ExternalJob> search(JobSearchCriteria criteria);
}
```

### Prompt

```text
Implement modular Job Discovery.

Create:

JobSource
ExternalJob
JobSearchCriteria
Job
JobSourceResult

Implement:
- source abstraction
- normalization
- deduplication
- canonical job URL
- source metadata
- job persistence
- search API
- job detail API

Start with a mock/demo source.

Do not bypass website restrictions or anti-bot mechanisms.

Keep every source modular so sources can be added or removed independently.

Add tests for normalization and duplicate detection.
```

---

## Phase 9 — AI Matching Engine

### Objective

Score jobs like a recruiter.

### Scoring

| Category | Weight |
|---|---:|
| Eligibility | 25 |
| Technical Skills | 25 |
| Relevant Experience | 15 |
| Role/Seniority | 10 |
| Education | 5 |
| Location/Work Mode | 5 |
| Project/Domain Relevance | 5 |
| Overall Recruiter Appeal | 10 |

### Classification

- 90–100: Excellent
- 80–89: Strong
- 70–79: Good
- 60–69: Possible
- <60: Low

### Recommendations

- APPLY
- REVIEW
- SKIP

### Prompt

```text
Implement the AI Matching Engine.

Use a multi-stage pipeline:

1. Hard eligibility filtering
2. Required skill matching
3. Keyword/skill matching
4. Semantic matching where supported
5. AI recruiter evaluation
6. Weighted score calculation
7. Recommendation

Use these weights exactly:

Eligibility 25
Technical Skills 25
Relevant Experience 15
Role/Seniority 10
Education 5
Location/Work Mode 5
Project/Domain Relevance 5
Overall Recruiter Appeal 10

Classification:

90-100 Excellent
80-89 Strong
70-79 Good
60-69 Possible
Below 60 Low

Recommendation:
APPLY
REVIEW
SKIP

Store score breakdown and reasoning.

Do not fabricate candidate facts.
Make the result auditable.

Add unit and integration tests.
```

---

## Phase 10 — Job Dashboard

### Objective

Connect the job UI to backend job and matching data.

### Prompt

```text
Connect the JobPilot AI frontend dashboard and jobs pages to the backend.

Implement:
- job search
- filters
- sorting
- pagination
- match score
- recommendation
- save job
- ignore job
- job details
- AI recruiter analysis
- loading states
- empty states
- errors

Use TanStack Query.

Preserve the existing visual design.

Do not redesign the pages.
```

---

# 10. Stage E — AI Job Search Agent

## Phase 11 — Job Search Agent

### Objective

Create the core AI job-search workflow.

### Workflow

```text
Candidate Profile
      ↓
Preferences
      ↓
Generate Search Strategies
      ↓
Generate Role Variations
      ↓
Search Sources
      ↓
Normalize
      ↓
Deduplicate
      ↓
Eligibility Filter
      ↓
AI Matching
      ↓
Rank
      ↓
Recommendations
```

### Prompt

```text
Implement the AI Job Search Agent.

Inputs:
- candidate profile
- job preferences

The agent should:
1. generate search strategies
2. generate role variations
3. search supported job sources
4. normalize results
5. deduplicate results
6. apply eligibility filters
7. calculate match scores
8. rank jobs
9. generate recommendations

Create a SearchRun entity and execution history.

Track:
- search start
- source
- query
- number found
- duplicates removed
- filtered jobs
- matched jobs
- errors
- completion time

Expose the results to the Agent Control Center.

Make the process auditable.
```

---

# 11. Stage F — Application Preparation

## Phase 12 — Application Preparation Agent

### Objective

Prepare an application without fabricating information.

### Prompt

```text
Implement the Application Preparation Agent.

For a selected job, generate:

- application-ready resume version where appropriate
- cover letter
- screening-question answers
- application summary
- missing information list

Rules:

- Never fabricate experience.
- Never fabricate education.
- Never fabricate certifications.
- Never fabricate employment history.
- Never invent answers to unknown screening questions.

Unknown or ambiguous questions must become:

REQUIRES_USER_INPUT

Create application preparation states.

Allow the user to review and edit all generated content.
```

---

## Phase 13 — Playwright Application Worker

### Objective

Create a separate browser automation service.

### Architecture

```text
Backend
   ↓
Application Job
   ↓
Playwright Worker
   ↓
Application Adapter
   ↓
Supported Website
```

### Interface

```java
interface ApplicationAdapter {
    boolean supports(Job job);
    ApplicationResult execute(ApplicationContext context);
}
```

### Prompt

```text
Create the separate application-worker service using Playwright.

Implement:

- BrowserManager
- ApplicationAdapter
- ApplicationAdapterRegistry
- ApplicationContext
- ApplicationResult
- worker queue integration
- screenshots/logging for debugging
- mock application adapter

Rules:

- Never bypass CAPTCHA.
- Never bypass MFA.
- Never bypass anti-bot mechanisms.
- Never bypass access controls.
- Stop when human intervention is required.
- Never fabricate candidate information.

Initially use a mock application flow for testing.

The worker must be independently deployable.
```

---

# 12. Stage G — Human-in-the-Loop

## Phase 14 — Human Intervention Center

### Objective

Handle situations requiring the candidate.

### Intervention Reasons

Examples:

- CAPTCHA
- MFA
- unknown screening question
- missing information
- unsupported form field
- consent/legal declaration
- ambiguous answer
- application flow failure

### Prompt

```text
Implement Human Intervention.

Create:

HumanIntervention entity.

Fields should include:
- application
- reason
- status
- required input
- context
- created time
- resolved time

Statuses:

PENDING
IN_PROGRESS
RESOLVED
CANCELLED

Create frontend Intervention Center.

The worker must pause when intervention is required.

Allow the user to:
- inspect context
- provide an answer
- resume
- cancel

Never automate CAPTCHA or MFA completion.
```

---

## Phase 15 — Controlled Auto-Apply

### Objective

Create deterministic and auditable auto-apply rules.

### Default Policy

```text
enabled = false
minimumScore = 85
requireApproval = true
maxApplicationsPerDay = 5
```

### Example Eligibility

```text
score >= 85
AND
no mandatory missing requirement
AND
location compatible
AND
experience compatible
AND
salary compatible
AND
supported application flow
AND
no unresolved intervention
```

### Prompt

```text
Implement Controlled Auto-Apply.

Create AutoApplyPolicy.

Default:

enabled = false
minimumScore = 85
requireApproval = true
maxApplicationsPerDay = 5

Create deterministic policy evaluation.

A job may only be auto-applied when all configured conditions are satisfied.

Every decision must record:
- score
- policy values
- passed conditions
- failed conditions
- decision
- timestamp

Default behavior must require explicit user approval before final submission.

Do not bypass CAPTCHA, MFA, anti-bot systems, or security controls.
```

---

# 13. Stage H — Application Tracking

## Phase 16 — Application Tracking

### Objective

Track the full application lifecycle.

### States

```text
DRAFT
PREPARING
READY_FOR_REVIEW
APPROVED
SUBMITTING
SUBMITTED
FAILED
REQUIRES_USER_INPUT
WITHDRAWN
REJECTED
INTERVIEW
OFFER
```

### Prompt

```text
Implement Application Tracking.

Create:
- Application
- ApplicationEvent
- ApplicationStatus

Store a complete lifecycle timeline.

Record:
- status
- timestamp
- source
- job
- submission result
- failure reason
- intervention
- user actions

Expose:
- application list
- application detail
- timeline
- filters
- statistics

Connect the tracker to the application worker.
```

---

# 14. Stage I — Notifications

## Phase 17 — Notifications

### Objective

Notify users about meaningful application and agent events.

### Events

- new high-match job
- intervention required
- application submitted
- application failed
- interview
- deadline
- important status change

### Prompt

```text
Implement notifications.

Create a notification abstraction.

Support:
- in-app notifications
- email notification provider abstraction

Create notification preferences.

Do not send unnecessary or duplicate notifications.

Add event-driven notification handling for:
- high-match jobs
- human interventions
- submissions
- failures
- interviews
- deadlines
```

---

# 15. Stage J — Scheduler & Continuous Agent

## Phase 18 — Scheduler

### Objective

Allow recurring job discovery.

### Prompt

```text
Implement scheduled JobPilot AI agent runs.

Create AgentSchedule.

Support:
- enabled/disabled
- frequency
- search preferences
- next run
- last run
- run history

Use Spring Scheduler.

Use distributed locking so multiple backend instances do not run the same schedule simultaneously.

Support:
- manual run
- scheduled run
- run status
- errors
- execution history

Do not create duplicate applications during repeated runs.
```

---

# 16. Stage K — Security

## Phase 19 — Security Hardening

### Prompt

```text
Perform a complete JobPilot AI security hardening pass.

Review:

- JWT handling
- refresh tokens
- authorization
- password security
- CORS
- CSRF where applicable
- rate limiting
- file validation
- upload size
- secure file storage
- secret management
- environment variables
- SQL injection risks
- XSS
- SSRF
- insecure direct object references
- sensitive logging
- API validation
- worker authentication
- service-to-service communication

Never log:
- passwords
- tokens
- resume contents
- private application answers
- sensitive user data

Add security tests.
```

---

# 17. Stage L — Observability

## Phase 20 — Observability

### Objective

Make the platform diagnosable.

### Track

- request ID
- user ID where appropriate
- search run ID
- application ID
- worker job ID
- AI request ID
- errors
- latency
- job-source failures
- worker failures

### Prompt

```text
Implement production observability.

Add structured logging and correlation IDs.

Track:
- HTTP request metrics
- job search runs
- source failures
- AI requests
- AI latency
- application execution
- worker health
- application failures
- scheduler runs

Do not log sensitive user information.

Add health and readiness endpoints.
```

---

# 18. Stage M — Testing

## Phase 21 — Full Test Strategy

### Backend

- JUnit
- Mockito
- Spring Boot Test
- Testcontainers

### Frontend

- Vitest
- React Testing Library

### Worker

- Playwright
- mock application environments

### Prompt

```text
Create a complete automated test strategy.

Backend:
- unit tests
- integration tests
- repository tests
- controller tests
- security tests
- Testcontainers for PostgreSQL/Redis

Frontend:
- component tests
- page tests
- form validation
- API state tests

Worker:
- mock application tests
- adapter tests
- intervention tests
- failure recovery tests

Test:
- authentication
- resume parsing
- candidate extraction
- preference validation
- job normalization
- duplicate detection
- matching
- application preparation
- intervention
- auto-apply policy
- tracking
- scheduler

Run the complete test suite and fix failures.
```

---

# 19. Stage N — Deployment

## Phase 22 — Production Deployment

### Target

```text
React/Vite
   ↓
Vercel

Spring Boot
   ↓
Render

PostgreSQL
   ↓
Managed PostgreSQL

Redis
   ↓
Managed Redis

Playwright Worker
   ↓
Separate Worker Service
```

### Prompt

```text
Prepare JobPilot AI for production deployment.

Frontend:
- production Vite build
- environment variables
- API URL configuration
- Vercel configuration

Backend:
- Dockerfile
- production configuration
- environment variables
- database migrations
- health checks
- graceful shutdown

Worker:
- production Dockerfile
- browser dependencies
- worker configuration
- secure backend communication

Add:
- CORS configuration
- HTTPS assumptions
- startup checks
- readiness checks
- error handling

Document deployment steps.

Do not hardcode secrets.
```

---

# 20. Stage O — Admin & Final Audit

## Phase 23 — Admin Dashboard

### Objective

Provide controlled operational visibility.

### Admin Features

- users
- job sources
- search runs
- AI usage
- applications
- worker health
- failures
- system status

### Prompt

```text
Implement an admin dashboard.

Restrict all admin routes using role-based authorization.

Display:
- users
- job source health
- search runs
- AI usage
- applications
- worker health
- failures
- scheduler status

Do not expose sensitive candidate information unnecessarily.

Add admin authorization tests.
```

---

## Phase 24 — Final Architecture Audit

### Prompt

```text
Perform a final production architecture audit of JobPilot AI.

Review:

- frontend architecture
- backend architecture
- database schema
- API contracts
- AI provider abstraction
- job source abstraction
- application adapter abstraction
- Playwright worker
- authentication
- authorization
- security
- error handling
- observability
- scheduler
- notifications
- application tracking
- deployment
- tests
- documentation

Create:

/docs/ARCHITECTURE_AUDIT.md
/docs/PRODUCTION_READINESS.md

Identify:
- architecture problems
- duplicated code
- missing validation
- security risks
- scalability risks
- reliability risks
- missing tests
- missing documentation

Fix issues where safe.

Run:
- frontend build
- frontend tests
- backend tests
- worker tests

Do not declare production-ready unless the actual checks pass.

Update IMPLEMENTATION_STATUS.md.
```

---

# 21. Core Domain Interfaces

Keep these abstractions stable.

## Job Sources

```java
public interface JobSource {

    String getSourceName();

    List<ExternalJob> search(JobSearchCriteria criteria);
}
```

## Application Adapters

```java
public interface ApplicationAdapter {

    boolean supports(Job job);

    ApplicationResult execute(ApplicationContext context);
}
```

## AI Providers

```java
public interface AIProvider {

    AIResponse generate(AIRequest request);
}
```

## Storage

```java
public interface FileStorageService {

    StoredFile store(FileUpload file);

    void delete(String fileId);

    StoredFile get(String fileId);
}
```

---

# 22. Application State Model

Use an explicit state machine.

```text
DRAFT
  ↓
PREPARING
  ↓
READY_FOR_REVIEW
  ↓
APPROVED
  ↓
SUBMITTING
  ↓
SUBMITTED
```

Alternative paths:

```text
PREPARING
   ↓
REQUIRES_USER_INPUT
   ↓
PREPARING
```

```text
SUBMITTING
   ↓
FAILED
```

```text
SUBMITTED
   ↓
INTERVIEW
   ↓
OFFER
```

---

# 23. Auto-Apply Safety Model

Default:

```text
Auto Apply Enabled: FALSE
Minimum Score: 85
Require Approval: TRUE
Daily Limit: 5
```

Never automatically:

- solve CAPTCHA
- bypass MFA
- bypass anti-bot systems
- bypass access controls
- fabricate answers
- fabricate experience
- fabricate qualifications
- submit unknown legal declarations without user review

---

# 24. AI Output Rules

All AI-generated structured outputs must:

1. Use strict schemas.
2. Be validated before persistence.
3. Be attributable to the AI operation that produced them.
4. Never be treated as verified candidate facts without evidence.
5. Distinguish known information from inference.
6. Mark uncertain information.
7. Require user input for unknown application questions.

---

# 25. Implementation Status

Maintain this file:

```text
/docs/IMPLEMENTATION_STATUS.md
```

Use:

```markdown
# JobPilot AI Implementation Status

## Design
- [ ] Phase 0 — Design Analysis

## Frontend
- [ ] Phase 1 — Frontend Foundation
- [ ] Phase 2 — Visual Screens

## Backend
- [ ] Phase 3 — Backend Foundation
- [ ] Phase 4 — Authentication
- [ ] Phase 5 — Resume Intelligence
- [ ] Phase 6 — Candidate Profile
- [ ] Phase 7 — Job Preferences
- [ ] Phase 8 — Job Discovery
- [ ] Phase 9 — AI Matching
- [ ] Phase 10 — Job Dashboard
- [ ] Phase 11 — Job Search Agent
- [ ] Phase 12 — Application Preparation
- [ ] Phase 13 — Playwright Worker
- [ ] Phase 14 — Human Intervention
- [ ] Phase 15 — Controlled Auto-Apply
- [ ] Phase 16 — Application Tracking
- [ ] Phase 17 — Notifications
- [ ] Phase 18 — Scheduler
- [ ] Phase 19 — Security
- [ ] Phase 20 — Observability
- [ ] Phase 21 — Testing
- [ ] Phase 22 — Deployment
- [ ] Phase 23 — Admin Dashboard
- [ ] Phase 24 — Final Architecture Audit
```

After each phase:

1. Verify implementation.
2. Run tests.
3. Run builds.
4. Fix errors.
5. Update the status.
6. Record important architectural decisions.
7. Only then proceed.

---

# 26. Antigravity Master Instruction

Use this instruction whenever working on JobPilot AI:

```text
You are working on JobPilot AI.

Before making changes:

1. Read /docs/MASTER_IMPLEMENTATION_PLAN.md
2. Read /docs/IMPLEMENTATION_STATUS.md
3. Read the relevant design documentation.
4. Inspect the existing code.
5. Determine the current phase.
6. Do not implement future phases unless explicitly instructed.

During implementation:

- Preserve existing architecture.
- Reuse existing components.
- Avoid unnecessary rewrites.
- Do not invent candidate information.
- Do not bypass security mechanisms.
- Keep modules independently testable.
- Keep external integrations behind interfaces.
- Keep AI providers behind an abstraction.
- Keep job sources modular.
- Keep application adapters modular.
- Keep Playwright separate from the main backend.

After implementation:

1. Run relevant tests.
2. Run builds.
3. Fix errors.
4. Check for regressions.
5. Update /docs/IMPLEMENTATION_STATUS.md.
6. Report exactly what changed.
7. Report tests/builds performed.
8. Report known limitations.

Do not claim success unless the relevant checks actually pass.
```

---

# 27. Immediate Execution Order

The first actions after creating this master plan are:

```text
1. Put all 7 images in /design-reference/
2. Open jobpilot-ai/ in Antigravity
3. Create IMPLEMENTATION_STATUS.md
4. Execute Phase 0 — Design Analysis
5. Review generated design documentation
6. Execute Phase 1 — Frontend Foundation
7. Implement the 7 screens one at a time
8. Verify frontend visually
9. Execute Phase 3 — Backend Foundation
10. Continue phase by phase
```

Do not start Playwright or auto-apply until the candidate, job, matching, application-preparation, and human-intervention foundations are working.

---

# 28. Definition of Done

JobPilot AI is considered ready for production only when:

- frontend build passes
- backend build passes
- worker build passes
- automated tests pass
- authentication is secure
- authorization is enforced
- resume uploads are validated
- candidate profile extraction is reviewable
- job sources are modular
- jobs are normalized and deduplicated
- matching is explainable
- application preparation never fabricates information
- human intervention works
- Playwright worker is isolated
- CAPTCHA/MFA are not bypassed
- auto-apply policy is deterministic
- application lifecycle is tracked
- notifications work
- scheduler is reliable
- secrets are externalized
- logs avoid sensitive information
- health checks work
- deployment is documented
- architecture audit is completed
- production-readiness documentation is completed
