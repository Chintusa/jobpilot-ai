// ============================================================
// Global TypeScript Types — JobPilot AI
// ============================================================

// Agent Types
export type AgentStatus = 'ACTIVE' | 'BUSY' | 'PAUSED' | 'INACTIVE' | 'ERROR';

export type PipelineStage =
  | 'DISCOVERED'
  | 'MATCHED'
  | 'PREPARING'
  | 'REVIEW'
  | 'SUBMITTED'
  | 'INTERVIEW'
  | 'OFFER';

export type ApplicationStatus =
  | 'DISCOVERED'
  | 'MATCHED'
  | 'PREPARING'
  | 'PENDING_REVIEW'
  | 'SUBMITTED'
  | 'INTERVIEWING'
  | 'OFFERED'
  | 'REJECTED'
  | 'WITHDRAWN';

// Job Types
export interface Skill {
  name: string;
  matched?: boolean;
}

export interface Job {
  id: string;
  title: string;
  company: string;
  companyLogoUrl?: string;
  location: string;
  workMode: 'REMOTE' | 'HYBRID' | 'ONSITE';
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: 'INR' | 'USD';
  salaryDisplay?: string;
  experience?: string;
  matchScore?: number;
  skills?: Skill[];
  missingSkills?: Skill[];
  isBookmarked?: boolean;
  postedAt?: string;
  source?: string;
  sourceName?: string;
  canonicalUrl?: string;
  description?: string;
}

export interface JobAssessment {
  matchScore: number;
  label: 'STRONG_MATCH' | 'GOOD_MATCH' | 'WEAK_MATCH';
  breakdown: {
    technicalSkills: number;
    experience: number;
    education: number;
    location: number;
    projects: number;
    roleFit: number;
  };
  profileSkills: Skill[];
  requiredSkills: Skill[];
  matchedSkills: Skill[];
  missingSkills: Skill[];
  whyYouMatch: string;
  potentialConcerns: string[];
}

export interface ApplicationReadiness {
  resume: 'READY' | 'MISSING' | 'NEEDS_UPDATE';
  candidateProfile: 'COMPLETE' | 'INCOMPLETE';
  coverLetter: 'GENERATED' | 'PENDING' | 'MISSING';
  screeningQuestions: { total: number; needReview: number };
  percentage: number;
}

// Application Types
export interface Application {
  id: string;
  jobTitle: string;
  company: string;
  companyLogoUrl?: string;
  location: string;
  salary?: string;
  matchScore: number;
  status: ApplicationStatus;
  appliedAt: string;
  lastUpdatedAt: string;
  submittedVia: 'AI_AGENT' | 'MANUAL';
}

export interface ActivityEntry {
  id: string;
  timestamp: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  isActive?: boolean;
}

export interface LogEntry {
  id: string;
  timestamp: string;
  message: string;
  level: 'info' | 'warning' | 'error';
}

// User Types
export interface UserProfile {
  id: string;
  name: string;
  email: string;
  avatarUrl?: string;
}

// Dashboard Types
export interface DashboardSummary {
  jobsFound: number;
  strongMatches: number;
  applications: number;
  interviews: number;
  agentStatus: AgentStatus;
  pipelineCounts: Record<PipelineStage, number>;
}

// Agent Config Types
export interface AgentConfig {
  jobDiscovery: {
    searchAutomatically: boolean;
    multipleSourceSearch: boolean;
    removeDuplicates: boolean;
    analyzeNewJobs: boolean;
  };
  applications: {
    prepareAutomatically: boolean;
    finalSubmission: 'AUTO' | 'USER_APPROVAL_REQUIRED';
    dailyApplicationLimit: number;
  };
  matching: {
    minimumMatchScore: number;
    requireAllMandatory: boolean;
  };
  humanIntervention: {
    pauseOnCaptcha: boolean;
    pauseOnMFA: boolean;
    pauseOnUnknownQuestions: boolean;
    pauseOnLegalDeclarations: boolean;
    pauseOnMissingInfo: boolean;
    pauseOnUnsupportedFlows: boolean;
  };
}

// Screening Question Types
export interface ScreeningQuestion {
  id: string;
  question: string;
  aiAnswer: string | null;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';
  status: 'ACCEPTED' | 'EDITED' | 'PENDING' | 'NEEDS_ANSWER';
  source?: string;
}

// Application Step Types
export interface ApplicationStep {
  label: string;
  status: 'completed' | 'active' | 'pending';
}

// Intervention Types
export interface Intervention {
  id: string;
  applicationId: string;
  jobTitle: string;
  company: string;
  type: 'CAPTCHA' | 'MFA' | 'UNKNOWN_QUESTION' | 'LEGAL' | 'MISSING_INFO' | 'UNSUPPORTED_FLOW';
  description: string;
  createdAt: string;
  status: 'PENDING' | 'RESOLVED' | 'SKIPPED';
}

// API Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  hasMore: boolean;
}
