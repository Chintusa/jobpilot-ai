import { Page } from 'playwright';

export type InterventionReason =
  | 'CAPTCHA'
  | 'MFA'
  | 'UNKNOWN_QUESTION'
  | 'LEGAL_DECLARATION'
  | 'MISSING_INFO'
  | 'UNSUPPORTED_FLOW'
  | 'ACCESS_CONTROL';

export type ApplicationStatus =
  | 'SUCCESS'
  | 'HUMAN_INTERVENTION_REQUIRED'
  | 'FAILED'
  | 'CANCELLED';

export interface CandidateData {
  fullName: string;
  email: string;
  phone: string;
  location: string;
  totalExperienceYears: number;
  currentTitle?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
  summary?: string;
}

export interface ScreeningAnswer {
  question: string;
  answer: string;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';
}

export interface ApplicationContext {
  applicationId: string;
  jobId: string;
  jobTitle: string;
  company: string;
  jobUrl: string;
  candidate: CandidateData;
  tailoredResumePath?: string;
  tailoredResumeContent?: string;
  coverLetter?: string;
  screeningAnswers: ScreeningAnswer[];
  sourceName?: string;
  options?: {
    headless?: boolean;
    simulateCaptcha?: boolean;
    simulateMfa?: boolean;
    simulateUnknownQuestion?: boolean;
  };
}

export interface ApplicationResult {
  applicationId: string;
  status: ApplicationStatus;
  interventionReason?: InterventionReason;
  interventionDescription?: string;
  confirmationId?: string;
  screenshots: string[]; // paths or base64
  logs: string[];
  executionTimeMs: number;
  error?: string;
}

export interface ApplicationAdapter {
  readonly name: string;
  canHandle(jobUrl: string, sourceName?: string): boolean;
  apply(page: Page, context: ApplicationContext): Promise<ApplicationResult>;
  detectIntervention(page: Page): Promise<{ required: boolean; reason?: InterventionReason; description?: string }>;
}
