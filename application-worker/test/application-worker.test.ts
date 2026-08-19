import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { BrowserManager } from '../src/browser/BrowserManager';
import { ApplicationAdapterRegistry } from '../src/adapters/ApplicationAdapterRegistry';
import { WorkerService } from '../src/service/WorkerService';
import { ApplicationContext } from '../src/types';

describe('Playwright Application Worker Service & Safety Guardrails', () => {
  let browserManager: BrowserManager;
  let adapterRegistry: ApplicationAdapterRegistry;
  let workerService: WorkerService;

  beforeAll(() => {
    browserManager = new BrowserManager();
    adapterRegistry = new ApplicationAdapterRegistry();
    workerService = new WorkerService(browserManager, adapterRegistry);
  });

  afterAll(async () => {
    await workerService.shutdown();
  });

  it('should discover and match MockApplicationAdapter via Registry', () => {
    const adapter = adapterRegistry.getAdapter('https://example.com/jobs/123', 'MOCK_DISCOVERY_ENGINE');
    expect(adapter).not.toBeNull();
    expect(adapter?.name).toBe('MOCK_PORTAL_ADAPTER');
  });

  it('should execute a complete successful application flow on mock portal', async () => {
    const context: ApplicationContext = {
      applicationId: 'test-app-001',
      jobId: 'job-101',
      jobTitle: 'Senior Java Backend Engineer',
      company: 'TechNova Technologies',
      jobUrl: 'https://example.com/jobs/technova-101',
      candidate: {
        fullName: 'Jane Developer',
        email: 'jane.dev@example.com',
        phone: '+91 9876543210',
        location: 'Bengaluru, India',
        totalExperienceYears: 3.5,
      },
      coverLetter: 'Dear Hiring Team, I am thrilled to apply for the Senior Java Engineer position.',
      screeningAnswers: [
        {
          question: 'Years of Java experience?',
          answer: '3.5 years',
          confidence: 'HIGH',
        },
      ],
      options: {
        headless: true,
      },
    };

    const result = await workerService.processApplication(context);

    expect(result.status).toBe('SUCCESS');
    expect(result.confirmationId).toBeDefined();
    expect(result.confirmationId).toContain('TECHNOVATECHNOLOGIES');
    expect(result.executionTimeMs).toBeGreaterThan(0);
    expect(result.logs.length).toBeGreaterThan(0);
  });

  it('RULE ENFORCEMENT: Never bypass CAPTCHA — must halt and require human intervention', async () => {
    const context: ApplicationContext = {
      applicationId: 'test-app-captcha',
      jobId: 'job-102',
      jobTitle: 'Cloud Backend Engineer',
      company: 'CloudScale Systems',
      jobUrl: 'https://example.com/jobs/cloudscale-102',
      candidate: {
        fullName: 'Jane Developer',
        email: 'jane.dev@example.com',
        phone: '+91 9876543210',
        location: 'Bengaluru, India',
        totalExperienceYears: 3.5,
      },
      screeningAnswers: [],
      options: {
        headless: true,
        simulateCaptcha: true,
      },
    };

    const result = await workerService.processApplication(context);

    expect(result.status).toBe('HUMAN_INTERVENTION_REQUIRED');
    expect(result.interventionReason).toBe('CAPTCHA');
    expect(result.interventionDescription).toContain('CAPTCHA challenge encountered');
  });

  it('RULE ENFORCEMENT: Never bypass MFA — must halt and require human intervention', async () => {
    const context: ApplicationContext = {
      applicationId: 'test-app-mfa',
      jobId: 'job-103',
      jobTitle: 'Distributed Systems Architect',
      company: 'NextGen AI Labs',
      jobUrl: 'https://example.com/jobs/nextgen-103',
      candidate: {
        fullName: 'Jane Developer',
        email: 'jane.dev@example.com',
        phone: '+91 9876543210',
        location: 'Bengaluru, India',
        totalExperienceYears: 3.5,
      },
      screeningAnswers: [],
      options: {
        headless: true,
        simulateMfa: true,
      },
    };

    const result = await workerService.processApplication(context);

    expect(result.status).toBe('HUMAN_INTERVENTION_REQUIRED');
    expect(result.interventionReason).toBe('MFA');
    expect(result.interventionDescription).toContain('MFA/OTP verification code');
  });

  it('RULE ENFORCEMENT: Never fabricate candidate info — halt when screening question is unknown', async () => {
    const context: ApplicationContext = {
      applicationId: 'test-app-unknown-question',
      jobId: 'job-104',
      jobTitle: 'Full Stack Java Engineer',
      company: 'FinPay Solutions',
      jobUrl: 'https://example.com/jobs/finpay-104',
      candidate: {
        fullName: 'Jane Developer',
        email: 'jane.dev@example.com',
        phone: '+91 9876543210',
        location: 'Hyderabad, India',
        totalExperienceYears: 3.5,
      },
      screeningAnswers: [
        {
          question: 'Do you possess an active DoD Secret clearance?',
          answer: '', // unverified
          confidence: 'UNKNOWN',
        },
      ],
      options: {
        headless: true,
      },
    };

    const result = await workerService.processApplication(context);

    expect(result.status).toBe('HUMAN_INTERVENTION_REQUIRED');
    expect(result.interventionReason).toBe('UNKNOWN_QUESTION');
    expect(result.interventionDescription).toContain('Unanswered question requiring candidate input');
  });
});
