# JobPilot AI — Production Deployment Guide

This guide outlines the standardized procedures for deploying JobPilot AI to a production environment.

## 🏗 Architecture Overview

The system consists of three distinct components:
1. **Frontend**: React/Vite SPA hosted on Vercel.
2. **Backend**: Spring Boot 3 / PostgreSQL API server.
3. **Application Worker**: Playwright-enabled Node.js background worker.

---

## 1. Deploying the Backend

The backend is packaged as an optimized Docker image (`backend/Dockerfile`).

### Prerequisites
- PostgreSQL 15+ database
- Redis cache/broker
- SMTP Server (for email notifications)

### Environment Variables
Configure the following required secrets in your container environment (e.g., AWS ECS, render.com, Kubernetes Secrets):

```env
DB_URL=jdbc:postgresql://<db-host>:5432/jobpilot
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=a_very_long_secure_random_string_here_256_bits
WORKER_API_KEY=secure_generated_api_key_for_worker
WORKER_URL=http://worker-internal-host:3001
CORS_ALLOWED_ORIGINS=https://jobpilot.yourdomain.com
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### Deployment Steps
1. Build the image: `docker build -t jobpilot-backend ./backend`
2. Push to your registry: `docker push your-registry/jobpilot-backend`
3. Deploy the container.
4. **Health Check**: Verify deployment at `https://api.yourdomain.com/actuator/health/readiness`. The API will automatically apply Flyway migrations on startup.

---

## 2. Deploying the Application Worker

The worker handles web scraping and automation and uses a specialized Microsoft Playwright base image.

### Environment Variables
```env
BACKEND_URL=http://backend-internal-host:8080
WORKER_API_KEY=secure_generated_api_key_for_worker
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### Deployment Steps
1. Build the image: `docker build -t jobpilot-worker ./application-worker`
2. Deploy the container alongside the backend. Ensure it has significant memory allocations (min 2GB RAM recommended) for concurrent browser contexts.

---

## 3. Deploying the Frontend (Vercel)

The frontend is optimized for zero-config deployments on Vercel.

### Environment Variables
```env
VITE_API_URL=https://api.yourdomain.com
```

### Deployment Steps
1. Connect your repository to Vercel.
2. Set the Framework Preset to **Vite**.
3. Set the Root Directory to `frontend`.
4. Add the `VITE_API_URL` environment variable.
5. Deploy. Vercel will automatically parse the included `vercel.json` to enforce strict security headers and proper React Router rewrites.

---

## 🛡 Security Checklist
- [ ] **No Hardcoded Secrets**: Ensure `application-prod.yml` and `.env.production` do not contain plaintext secrets in version control.
- [ ] **CORS**: Verify `CORS_ALLOWED_ORIGINS` exactly matches your frontend domain.
- [ ] **HTTPS**: Ensure your load balancers enforce HTTPS. The frontend `vercel.json` enforces HSTS automatically.
- [ ] **Graceful Shutdown**: The Spring Boot app supports graceful shutdowns (`server.shutdown: graceful`). Ensure your orchestrator waits at least 30 seconds before sending SIGKILL.
