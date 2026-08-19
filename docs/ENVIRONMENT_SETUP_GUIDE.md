# JobPilot AI — Complete Environment Setup & Configuration Guide

This guide provides step-by-step instructions for obtaining, configuring, and testing every variable in [.env.example](file:///c:/Users/jhask/Downloads/jobpilot-ai/.env.example).

---

## Table of Contents
1. [PostgreSQL Database Setup](#1-postgresql-database-setup)
2. [Redis Distributed Cache & Broker](#2-redis-distributed-cache--broker)
3. [Security & Authentication Secrets](#3-security--authentication-secrets)
4. [AI Model Provider Integration Keys](#4-ai-model-provider-integration-keys)
5. [Email Notification Provider (SMTP / SendGrid / AWS SES)](#5-email-notification-provider)
6. [Frontend Web Application Configuration](#6-frontend-web-application-configuration)
7. [Playwright Automation Worker Configuration](#7-playwright-automation-worker-configuration)
8. [Complete Environment Quick-Reference Matrix](#8-complete-environment-quick-reference-matrix)

---

## 1. PostgreSQL Database Setup

JobPilot AI uses PostgreSQL 15+ to store user profiles, candidate intelligence ontologies, job listings, applications, and audit logs.

### Option A: Local Development (Docker Compose — Recommended)
If you are running locally, you don't need to install PostgreSQL manually. Use the provided Docker Compose stack:

```bash
docker compose -f infrastructure/docker-compose.yml up -d postgres
```

Configure in `.env`:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jobpilot_db
SPRING_DATASOURCE_USERNAME=jobpilot_user
SPRING_DATASOURCE_PASSWORD=jobpilot_password
```

### Option B: Cloud Production Database (Supabase / Neon / AWS RDS)

#### 1. Supabase (Free & Instant)
1. Go to [supabase.com](https://supabase.com) and create a new project.
2. Under **Project Settings** $\rightarrow$ **Database**, copy the **Connection string** (URI/JDBC).
3. Set the variables:
   ```env
   DB_URL=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
   DB_USERNAME=postgres
   DB_PASSWORD=<your-supabase-password>
   ```

#### 2. Neon Serverless Postgres
1. Go to [neon.tech](https://neon.tech) and create a database.
2. Copy the JDBC connection URL.
3. Set the variables:
   ```env
   DB_URL=jdbc:postgresql://ep-xyz.us-east-2.aws.neon.tech/neondb?sslmode=require
   DB_USERNAME=<neon-user>
   DB_PASSWORD=<neon-password>
   ```

> [!TIP]
> Flyway database migrations run automatically on application startup. You do not need to run SQL DDL scripts manually.

---

## 2. Redis Distributed Cache & Broker

Redis handles rate limiting (Bucket4j), token blacklisting, caching job search queries, and worker queue messaging.

### Option A: Local Development (Docker Compose)
```bash
docker compose -f infrastructure/docker-compose.yml up -d redis
```

Configure in `.env`:
```env
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

### Option B: Cloud Production (Upstash Serverless Redis / AWS ElastiCache)

#### Upstash Redis (Free & Serverless)
1. Go to [upstash.com](https://upstash.com) $\rightarrow$ Create Database.
2. Select your closest region (e.g., `us-east-1`).
3. Copy the **Endpoint** and **Password**.
4. Configure in `.env`:
   ```env
   REDIS_HOST=your-endpoint.upstash.io
   REDIS_PORT=6379
   REDIS_PASSWORD=your_upstash_password_here
   ```

---

## 3. Security & Authentication Secrets

### 3.1 `JWT_SECRET` (256-bit Cryptographic Signing Key)
- **Purpose**: Signs and cryptographically verifies HMAC-SHA256 stateless JWT access tokens and refresh tokens.
- **Generation Step**:
  Run this command in terminal to generate a secure random 256-bit (32 bytes) hex key:
  ```bash
  # On Linux/macOS/Git Bash:
  openssl rand -hex 32

  # Or in PowerShell:
  -join ((1..32) | ForEach-Object { '{0:x2}' -f (Get-Random -Max 256) })
  ```
- **Example Value**:
  ```env
  JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  ```

### 3.2 `CORS_ALLOWED_ORIGINS`
- **Purpose**: Prevents Cross-Origin Resource Sharing attacks by whitelisting trusted frontend domains.
- **Development**:
  ```env
  CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
  ```
- **Production**:
  ```env
  CORS_ALLOWED_ORIGINS=https://jobpilot.yourdomain.com,https://app.jobpilot.ai
  ```

### 3.3 `WORKER_API_KEY` & `WORKER_URL`
- **Purpose**: A shared secret header (`X-Worker-Api-Key`) for internal communication between the Playwright background automation worker and the Spring Boot backend.
- **Generation Step**:
  ```bash
  openssl rand -hex 24
  ```
- **Configuration**:
  ```env
  WORKER_API_KEY=jobpilot_sec_worker_8f92a1c09d3b4e72
  WORKER_URL=http://localhost:3001
  ```

---

## 4. AI Model Provider Integration Keys

JobPilot AI includes a modular provider architecture. You can run locally with zero API cost using `standard` (built-in deterministic intelligence engine) or connect live cloud LLMs.

### 4.1 `AI_DEFAULT_PROVIDER`
Controls which engine handles resume parsing, fact-grounded cover letters, and screening questions.
- Supported values: `standard`, `mock`, `openai`, `gemini`, `anthropic`.

```env
AI_DEFAULT_PROVIDER=standard
```

---

### 4.2 OpenAI API Setup (`OPENAI_API_KEY`)
1. Go to [platform.openai.com](https://platform.openai.com/api-keys).
2. Log in and click **Create new secret key**.
3. Name it `jobpilot-production`.
4. Copy the key (`sk-proj-...`).
5. Configure in `.env`:
   ```env
   AI_DEFAULT_PROVIDER=openai
   OPENAI_API_KEY=sk-proj-abc123xyz456...
   ```

---

### 4.3 Google Gemini API Setup (`GEMINI_API_KEY`)
1. Go to [aistudio.google.com](https://aistudio.google.com/app/apikey).
2. Click **Create API key** (or select an existing Google Cloud Project).
3. Copy the key (`AIzaSy...`).
4. Configure in `.env`:
   ```env
   AI_DEFAULT_PROVIDER=gemini
   GEMINI_API_KEY=AIzaSyB123456789...
   ```

---

### 4.4 Anthropic Claude API Setup (`ANTHROPIC_API_KEY`)
1. Go to [console.anthropic.com](https://console.anthropic.com/settings/keys).
2. Click **Create Key**.
3. Copy the key (`sk-ant-...`).
4. Configure in `.env`:
   ```env
   AI_DEFAULT_PROVIDER=anthropic
   ANTHROPIC_API_KEY=sk-ant-api03-...
   ```

---

## 5. Email Notification Provider

JobPilot AI sends transactional notifications for:
- High-match job alerts ($\ge 90\%$ match)
- Required human interventions (CAPTCHA / MFA / Missing candidate answers)
- Application submission confirmations

### Option A: SendGrid (Recommended for Production)
1. Create an account at [sendgrid.com](https://sendgrid.com).
2. Go to **Settings** $\rightarrow$ **API Keys** $\rightarrow$ **Create API Key** (Full Access to "Mail Send").
3. Verify a Single Sender Identity or Domain at **Settings** $\rightarrow$ **Sender Authentication**.
4. Configure in `.env`:
   ```env
   SPRING_MAIL_HOST=smtp.sendgrid.net
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=apikey
   SPRING_MAIL_PASSWORD=SG.your_sendgrid_api_key_here
   NOTIFICATION_EMAIL_FROM=notifications@yourdomain.com
   ```

### Option B: Amazon Simple Email Service (AWS SES)
1. In AWS Console, navigate to **Amazon SES** $\rightarrow$ **SMTP Settings**.
2. Click **Create SMTP Credentials**.
3. Configure in `.env`:
   ```env
   SPRING_MAIL_HOST=email-smtp.us-east-1.amazonaws.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=your_ses_smtp_username
   SPRING_MAIL_PASSWORD=your_ses_smtp_password
   NOTIFICATION_EMAIL_FROM=notifications@yourverifieddomain.com
   ```

### Option C: Gmail SMTP (For Development Testing)
1. Go to your Google Account $\rightarrow$ **Security** $\rightarrow$ **2-Step Verification**.
2. Scroll to the bottom $\rightarrow$ **App passwords** $\rightarrow$ Create password named `JobPilot`.
3. Configure in `.env`:
   ```env
   SPRING_MAIL_HOST=smtp.gmail.com
   SPRING_MAIL_PORT=587
   SPRING_MAIL_USERNAME=your_email@gmail.com
   SPRING_MAIL_PASSWORD=your_16_char_app_password
   NOTIFICATION_EMAIL_FROM=your_email@gmail.com
   ```

---

## 6. Frontend Web Application Configuration

The React 19 SPA runs with Vite.

### 6.1 `VITE_API_URL`
- **Purpose**: Points the client-side API requests to the backend Spring Boot server.
- **Local Development**:
  ```env
  VITE_API_URL=http://localhost:8080
  ```
- **Production (Vercel / Cloudflare Pages)**:
  ```env
  VITE_API_URL=https://api.yourdomain.com
  ```

### 6.2 Optional Analytics & Error Monitoring
- `VITE_ANALYTICS_ID`: Your PostHog / Google Analytics Tracking ID (e.g. `G-XXXXXXXXXX`).
- `VITE_SENTRY_DSN`: Your Sentry DSN for frontend exception tracing (e.g. `https://xyz@sentry.io/12345`).

---

## 7. Playwright Automation Worker Configuration

The background worker operates headless browser sessions to perform job applications and capture verification screenshots.

```env
# URL to connect to the backend REST API
BACKEND_URL=http://localhost:8080

# Must match the WORKER_API_KEY on the backend
WORKER_API_KEY=jobpilot_worker_internal_secret_key_change_in_prod

# Logging level (debug, info, warn, error)
LOG_LEVEL=info

# Set to true in production Docker containers, false if you want to watch the browser in dev
HEADLESS=true

# Worker local listening port
PORT=3001
```

---

## 8. Complete Environment Quick-Reference Matrix

| Variable | Required In Dev | Required In Prod | How to Get It |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | Optional (default works) | Yes (`DB_URL`) | Docker Compose or Cloud PostgreSQL (RDS/Supabase/Neon) |
| `SPRING_DATASOURCE_USERNAME` | Optional | Yes (`DB_USERNAME`) | Database admin username |
| `SPRING_DATASOURCE_PASSWORD` | Optional | Yes (`DB_PASSWORD`) | Database admin password |
| `SPRING_DATA_REDIS_HOST` | Optional | Yes (`REDIS_HOST`) | Docker Compose or Upstash / AWS ElastiCache |
| `REDIS_PASSWORD` | No | If configured | Redis authentication password |
| `JWT_SECRET` | Optional (default works) | **MANDATORY** | Run `openssl rand -hex 32` |
| `CORS_ALLOWED_ORIGINS` | Optional | **MANDATORY** | Your production frontend URL (e.g. `https://jobpilot.yourdomain.com`) |
| `WORKER_API_KEY` | Optional | **MANDATORY** | Run `openssl rand -hex 24` |
| `WORKER_URL` | Optional | Yes | Internal URL of Playwright worker container |
| `AI_DEFAULT_PROVIDER` | Optional (`standard`) | Yes | Choose `standard`, `openai`, `gemini`, or `anthropic` |
| `OPENAI_API_KEY` | No | If using OpenAI | [platform.openai.com/api-keys](https://platform.openai.com/api-keys) |
| `GEMINI_API_KEY` | No | If using Gemini | [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey) |
| `ANTHROPIC_API_KEY` | No | If using Claude | [console.anthropic.com/settings/keys](https://console.anthropic.com/settings/keys) |
| `SPRING_MAIL_HOST` | No | If sending emails | SendGrid / AWS SES / Gmail SMTP host |
| `SPRING_MAIL_USERNAME` | No | If sending emails | SMTP username (e.g. `apikey`) |
| `SPRING_MAIL_PASSWORD` | No | If sending emails | SMTP password / API key |
| `VITE_API_URL` | Optional (`http://localhost:8080`) | **MANDATORY** | Backend API URL (e.g. `https://api.yourdomain.com`) |
| `BACKEND_URL` | Optional | Yes | Backend URL for Playwright worker |
| `HEADLESS` | Optional (`true`) | Yes (`true`) | Set to `true` for headless container execution |

---

## 9. Launching the App with Your `.env`

Once your `.env` is populated:

1. **Start Database & Redis**:
   ```bash
   docker compose -f infrastructure/docker-compose.yml up -d
   ```
2. **Start Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. **Start Frontend**:
   ```bash
   cd frontend
   npm run dev
   ```
4. **Start Application Worker**:
   ```bash
   cd application-worker
   npm run dev
   ```

- Frontend is accessible at: **`http://localhost:5173`**
- Backend API & Swagger UI: **`http://localhost:8080/swagger-ui.html`**
- Health Probe: **`http://localhost:8080/actuator/health`**
