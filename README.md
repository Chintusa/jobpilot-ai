# JobPilot AI — Autonomous Job Search, Recruiter Matching & Application Preparation Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.0-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8.2-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Playwright](https://img.shields.io/badge/Playwright-Chromium-brightgreen.svg)](https://playwright.dev/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

JobPilot AI is an enterprise-grade platform designed for automated job discovery, multi-dimensional recruiter matching, fact-grounded application tailoring, and human-in-the-loop browser automation.

---

## 🌟 Key Features

- **Candidate Intelligence**: PDF/DOCX parsing (Apache Tika) and structured entity extraction into a verified candidate profile ontology.
- **Recruiter Match Engine**: 8-dimension weighted matching algorithm (skills, experience, title, salary, location, work mode, education, seniority) with breakdown telemetry.
- **Application Preparation Agent**: Generates tailored resumes, fact-grounded cover letters, and screening question answers with confidence levels (`HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`).
- **Lifecycle State Machine**: Strict transition flow (`DRAFT` $\rightarrow$ `PREPARING` $\rightarrow$ `READY_FOR_REVIEW` $\rightarrow$ `APPROVED` $\rightarrow$ `SUBMITTING` $\rightarrow$ `SUBMITTED`) with full immutable audit timelines.
- **Safety & Anti-Abuse Guardrails**: Zero-fabrication halt for unknown questions, strict human intervention on CAPTCHA/MFA challenges, and quota-limited auto-apply policies.
- **Playwright Automation Worker**: Independent Node.js service executing browser-based portal submissions with timestamped audit screenshots.
- **Enterprise Security**: Stateless JWT auth, BCrypt hashing, Bucket4j rate limiting, CORS whitelisting, and CSP/HSTS headers.

---

## 🏗️ Architecture & Project Structure

```text
jobpilot-ai/
├── backend/                  # Java 21 / Spring Boot 3.3.3 REST API
│   ├── src/main/java/com/jobpilot/
│   │   ├── admin/            # Administration & system metrics
│   │   ├── ai/               # AI Provider abstraction & structured generation
│   │   ├── applications/     # Application lifecycle, preparation agent, state machine
│   │   ├── auth/             # JWT authentication, RBAC, password encryption
│   │   ├── autoapply/        # Quota-limited auto-apply policy engine
│   │   ├── automation/       # Human interventions & agent scheduling
│   │   ├── candidate/        # Structured candidate profile & ontology
│   │   ├── common/           # Shared entity models, DTO wrappers, exceptions
│   │   ├── jobs/             # Job sources, discovery engine, normalization
│   │   ├── matching/         # Recruiter match engine & scoring algorithms
│   │   ├── notifications/    # In-app and email notification system
│   │   ├── preferences/      # Candidate job search & auto-apply preferences
│   │   ├── resume/           # Resume parser, storage abstraction, AI extraction
│   │   └── security/         # Spring Security 6, rate limits, API key filters
│   └── src/main/resources/
│       ├── db/migration/     # Flyway database migrations (V1 to V7)
│       └── application.yml   # Multi-profile Spring configuration
│
├── frontend/                 # React 19 / Vite 8 / TypeScript SPA
│   └── src/
│       ├── api/              # Axios REST client with token refresh interceptors
│       ├── components/       # Atomic UI system (Buttons, Cards, Badges, Modals)
│       ├── features/         # Domain components (Match rings, agent status, screening cards)
│       ├── hooks/            # TanStack React Query hooks
│       ├── pages/            # 13 Interactive views (Dashboard, Jobs, Agent, Tracker, Profile, Admin)
│       └── routes/           # Protected routes & RBAC navigation shell
│
├── application-worker/       # Playwright browser automation service
│   └── src/
│       ├── adapters/         # Portal application adapters
│       ├── logger/           # Winston structured audit logger
│       └── service/          # Automation runner with CAPTCHA/MFA safety halts
│
├── infrastructure/           # Docker Compose (PostgreSQL 16, Redis 7)
├── docs/                     # Production architecture, setup guides & runbooks
├── .env.example              # Centralized environment template
└── DEPLOYMENT.md             # Production cloud deployment manual
```

---

## 🚀 Quick Start (Local Development)

### 1. Prerequisites
- **Java 21 LTS** & **Maven 3.9+**
- **Node.js 20+** & **npm**
- **Docker & Docker Compose**

### 2. Configure Environment
```bash
cp .env.example .env
```

### 3. Start Database & Redis
```bash
docker compose -f infrastructure/docker-compose.yml up -d
```

### 4. Start Spring Boot Backend (Port 8080)
```bash
cd backend
mvn spring-boot:run
```
- Health Probes: `http://localhost:8080/actuator/health`
- Swagger UI / OpenAPI: `http://localhost:8080/swagger-ui.html`

### 5. Start Frontend Web App (Port 5173)
```bash
cd frontend
npm install
npm run dev
```
- Open in browser: `http://localhost:5173`

### 6. Start Playwright Worker (Port 3001)
```bash
cd application-worker
npm install
npm run dev
```

---

## 🧪 Running Test Suites

```bash
# Backend Tests (67 tests, 0 failures)
cd backend && mvn test

# Frontend Tests (5 tests, 4 test files)
cd frontend && npm test

# Frontend Production Build
cd frontend && npm run build

# Application Worker Tests (12 tests, 5 test files)
cd application-worker && npm test
```

---

## 📖 Documentation & Guides

- [ENVIRONMENT_SETUP_GUIDE.md](docs/ENVIRONMENT_SETUP_GUIDE.md) — Step-by-step setup for every `.env` key & secret
- [ARCHITECTURE_AUDIT.md](docs/ARCHITECTURE_AUDIT.md) — 19-dimension architectural audit & benchmarks
- [PRODUCTION_READINESS.md](docs/PRODUCTION_READINESS.md) — Operational runbook & checklist
- [STAGES_LIFECYCLE.md](docs/STAGES_LIFECYCLE.md) — Application state machine specification
- [DEPLOYMENT.md](DEPLOYMENT.md) — Cloud deployment manual (Vercel, AWS, Render, GCP)

---

## 📄 License
This project is licensed under the MIT License.
