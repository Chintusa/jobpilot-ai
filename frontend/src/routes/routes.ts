export const ROUTES = {
  LANDING: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  JOBS: '/jobs',
  JOB_DETAIL: '/jobs/:id',
  AGENT: '/agent',
  AGENT_APPLY: '/agent/apply/:jobId',
  APPLICATIONS: '/applications',
  APPLICATION_DETAIL: '/applications/:id',
  INTERVENTIONS: '/interventions',
  PROFILE: '/profile',
  RESUME: '/resume',
  ANALYTICS: '/analytics',
  SETTINGS: '/settings',
  ADMIN: '/admin',
} as const;

export function jobDetailPath(id: string) { return `/jobs/${id}`; }
export function agentApplyPath(jobId: string) { return `/agent/apply/${jobId}`; }
export function applicationDetailPath(id: string) { return `/applications/${id}`; }
