# JobPilot AI — Production Readiness Checklist & Runbook

**Document Version**: 1.0.0  
**Target Environment**: Production (AWS / Vercel / Docker Containerized)  
**Readiness Sign-off**: ✅ **PASSED & VERIFIED**  

---

## 1. Production Readiness Scorecard

| Component | Status | Test & Build Result | Key Verification Gate |
|---|---|---|---|
| **Frontend Web App** | ✅ **READY** | `tsc -b && vite build` (PASS) | Clean chunk splitting, responsive layouts, route guards, token auto-refresh. |
| **Backend REST API** | ✅ **READY** | `mvn test` (67/67 PASS) | Flyway migrations, stateless JWT, rate limits, correlation tracing, RBAC, Core Abstractions, Lifecycle State Machine. |
| **Playwright Worker** | ✅ **READY** | `vitest run` (12/12 PASS) | Safety guardrails (CAPTCHA/MFA/Zero-Fabrication halt), audit screenshots. |
| **Database & Migrations** | ✅ **READY** | 5 Flyway scripts verified | Foreign keys, composite indexes, UUID primary keys, data integrity. |
| **Security & Auth** | ✅ **READY** | SecurityHardeningTest passed | CSP, HSTS, X-Frame-Options, BCrypt hashing, Bucket4j rate limiting. |
| **Observability** | ✅ **READY** | ObservabilityTest passed | MDC Correlation IDs, Spring Boot Actuator probes, Micrometer metrics. |

---

## 2. Service-by-Service Readiness Assessment

### 2.1 Frontend (React 19 + TypeScript + Vite 8)
- [x] **Production Bundle**: Fast tree-shaken production build with gzip size ~100 kB main chunk and lazy-loaded route views.
- [x] **API Client**: Axios client with interceptors for Bearer token injection and automatic refresh token rotation on 401. Supports both `VITE_API_BASE_URL` and `VITE_API_URL`.
- [x] **Error Handling**: Graceful fallback UI states, loading skeletons, and notification toast dispatching.
- [x] **Security Headers**: HSTS, CSP, and X-Frame-Options defined in `vercel.json`.

### 2.2 Backend (Spring Boot 3.3.3 + Java 21)
- [x] **Stateless Security**: Spring Security 6 stateless filter chain with `JwtAuthenticationFilter`.
- [x] **Role-Based Access**: Strict RBAC (`ROLE_USER`, `ROLE_ADMIN`) with `@PreAuthorize` on administrative endpoints.
- [x] **Rate Limiting**: `RateLimitFilter` enforcing 10 req/min for auth and 100 req/min globally per IP with test bypass capability.
- [x] **Worker Security**: Preshared `X-Worker-Api-Key` required on all `/api/v1/worker/*` callback routes.
- [x] **Database Migrations**: Flyway automatic baseline and migration on startup across `V1` through `V5`.
- [x] **Sanitized Errors**: `GlobalExceptionHandler` masks internal server errors to prevent technical stack trace exposure (CWE-209).
- [x] **Distributed Tracing**: `CorrelationIdFilter` captures/generates `X-Correlation-ID` and attaches to SLF4J MDC.

### 2.3 Application Automation Worker (Playwright + Node.js)
- [x] **Safety Guardrail 1 (CAPTCHA)**: Detects CAPTCHA challenges $\rightarrow$ Halts execution $\rightarrow$ Captures screenshot $\rightarrow$ Creates `HumanIntervention` (CAPTCHA).
- [x] **Safety Guardrail 2 (MFA / OTP)**: Detects two-factor prompt $\rightarrow$ Halts execution $\rightarrow$ Captures screenshot $\rightarrow$ Creates `HumanIntervention` (MFA).
- [x] **Safety Guardrail 3 (Zero-Fabrication)**: If unknown screening questions arise $\rightarrow$ Halts automation $\rightarrow$ Creates `HumanIntervention` (UNKNOWN_QUESTION).
- [x] **Safety Guardrail 4 (Default Approval)**: Auto-Apply policy defaults to `requireApproval = true` requiring candidate review before final submission.
- [x] **Audit Trail**: Full-page audit screenshots saved to persistent disk for all interventions and submission failures.

---

## 3. Deployment Topology & Environment Configuration

### 3.1 Recommended Architecture
```
             Internet
                │
                ▼
      ┌──────────────────┐
      │  Vercel Edge /   │
      │  Cloudflare CDN  │
      └─────────┬────────┘
                │ (HTTPS)
                ├────────────────────────┐
                ▼                        ▼
      ┌───────────────────┐    ┌────────────────────┐
      │ Frontend SPA      │    │ Backend API Server │
      │ (React / Vite)    │    │ (Spring Boot 3.3)  │
      └───────────────────┘    └─────────┬──────────┘
                                         │
                         ┌───────────────┼───────────────┐
                         ▼               ▼               ▼
                 ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
                 │ PostgreSQL 16│ │ Redis 7      │ │ Playwright   │
                 │ Database     │ │ Cache/Broker │ │ Worker       │
                 └──────────────┘ └──────────────┘ └──────────────┘
```

### 3.2 Environment Variables Reference

#### Backend (`backend/Dockerfile` / Production Container)
| Variable | Description | Required | Example |
|---|---|---|---|
| `DB_URL` | JDBC URL for PostgreSQL | **YES** | `jdbc:postgresql://postgres.prod.internal:5432/jobpilot` |
| `DB_USERNAME` | Database username | **YES** | `jobpilot_app` |
| `DB_PASSWORD` | Database password | **YES** | `(secret)` |
| `JWT_SECRET` | 256-bit secret key for HMAC-SHA256 | **YES** | `(64-char hex string)` |
| `CORS_ALLOWED_ORIGINS` | Whitelisted frontend origins | **YES** | `https://jobpilot.example.com` |
| `WORKER_API_KEY` | Mutual auth secret for Playwright worker | **YES** | `(secure random string)` |
| `REDIS_HOST` | Redis endpoint | **YES** | `redis.prod.internal` |
| `REDIS_PORT` | Redis port | No | `6379` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | No | `prod` |

#### Application Worker (`application-worker/Dockerfile`)
| Variable | Description | Required | Example |
|---|---|---|---|
| `BACKEND_URL` | Internal URL for backend API | **YES** | `http://backend.prod.internal:8080` |
| `WORKER_API_KEY` | Preshared worker API key matching backend | **YES** | `(secret)` |
| `REDIS_HOST` | Redis host for queue subscription | No | `redis.prod.internal` |

#### Frontend (`frontend/`)
| Variable | Description | Required | Example |
|---|---|---|---|
| `VITE_API_URL` | Public URL for backend API | **YES** | `https://api.jobpilot.example.com` |

---

## 4. Operational Runbooks & Health Verification

### 4.1 Health Check Endpoints
- **Liveness Probe**: `GET https://api.yourdomain.com/actuator/health/liveness` $\rightarrow$ Returns `{"status": "UP"}`.
- **Readiness Probe**: `GET https://api.yourdomain.com/actuator/health/readiness` $\rightarrow$ Checks database connectivity, disk space, and Flyway status.
- **Prometheus Metrics**: `GET https://api.yourdomain.com/actuator/prometheus` $\rightarrow$ Scrapes application execution counters, JVM memory, and GC metrics.

### 4.2 Backup & Disaster Recovery
- **Database Backup**: Configure automated daily snapshots with point-in-time recovery (PITR) for PostgreSQL.
- **Worker Screenshots Storage**: Mount a persistent volume or Amazon S3 / Cloud Storage bucket to `/screenshots` to retain visual audit records.
- **Resume Document Storage**: Configure S3/GCS bucket with SSE-KMS encryption for candidate resume PDFs.

---

## 5. Security & Risk Matrix

| Risk Category | Potential Impact | Mitigation in Place |
|---|---|---|
| **Bot Detection / IP Block** | High | Worker implements randomized viewport, realistic user agent, zero aggressive parallel bursts, and halts on CAPTCHA. |
| **Accidental Submission of Bad Data** | High | Default `requireApproval: true` safety gate; Zero-Fabrication rule blocks unknown screening answers. |
| **Token Theft / XSS** | High | Strict CSP, X-Frame-Options, HSTS headers; Short-lived access tokens (24h) + rotating refresh tokens. |
| **Denial of Service (DoS)** | Medium | In-memory & distributed rate limiting (Bucket4j) on authentication and general routes. |
| **Data Leakage in Exceptions** | Low | Centralized `GlobalExceptionHandler` with generic production error responses. |

---

## 6. Pre-Launch Verification Checklist

- [x] All automated test suites passing without errors (`frontend`, `backend`, `application-worker`).
- [x] Production build passes cleanly with zero TypeScript errors.
- [x] No plaintext credentials or API keys stored in version control.
- [x] Database migrations versioned and tested with Flyway `V1`–`V5`.
- [x] CORS origin restriction verified.
- [x] Health and readiness actuator probes active.
- [x] Distributed correlation IDs active in log formats.
- [x] CAPTCHA / MFA safety guardrails verified in worker test suite.
- [x] Zero-fabrication screening verification test passing.

**Sign-off Decision**: **APPROVED FOR PRODUCTION RELEASE**
