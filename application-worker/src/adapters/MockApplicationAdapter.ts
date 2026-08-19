import { Page } from 'playwright';
import { ApplicationAdapter, ApplicationContext, ApplicationResult, InterventionReason } from '../types';
import { logger } from '../logger';
import * as path from 'path';
import * as fs from 'fs';

export class MockApplicationAdapter implements ApplicationAdapter {
  readonly name = 'MOCK_PORTAL_ADAPTER';

  canHandle(jobUrl: string, sourceName?: string): boolean {
    return (
      jobUrl.includes('example.com') ||
      jobUrl.includes('mock') ||
      sourceName === 'MOCK_DISCOVERY_ENGINE' ||
      !jobUrl.startsWith('http')
    );
  }

  async detectIntervention(page: Page): Promise<{ required: boolean; reason?: InterventionReason; description?: string }> {
    // 1. Check for CAPTCHA
    const captchaElements = await page.locator('iframe[src*="captcha"], .g-recaptcha, #cf-turnstile, .h-captcha').count();
    if (captchaElements > 0) {
      return {
        required: true,
        reason: 'CAPTCHA',
        description: 'Automated portal triggered CAPTCHA challenge. Automation stopped to comply with safety rules.',
      };
    }

    // 2. Check for MFA / 2FA Challenge
    const mfaElements = await page.locator('input[name="mfaCode"], input[name="otp"], #two-factor-auth').count();
    if (mfaElements > 0) {
      return {
        required: true,
        reason: 'MFA',
        description: 'Multi-factor authentication (MFA/OTP) required from candidate device.',
      };
    }

    // 3. Check for Anti-bot / Access Control Block
    const accessBlocked = await page.locator('text=Access Denied, text=Please verify you are a human, text=Security Check').count();
    if (accessBlocked > 0) {
      return {
        required: true,
        reason: 'ACCESS_CONTROL',
        description: 'Access control security challenge detected.',
      };
    }

    return { required: false };
  }

  async apply(page: Page, context: ApplicationContext): Promise<ApplicationResult> {
    const startTime = Date.now();
    const logs: string[] = [];
    const screenshots: string[] = [];

    const logStep = (msg: string) => {
      logger.info(`[${context.applicationId}] ${msg}`);
      logs.push(`[${new Date().toISOString()}] ${msg}`);
    };

    logStep(`Starting application workflow for ${context.jobTitle} at ${context.company}`);

    try {
      // Step 0: Check for simulated intervention triggers (for deterministic test verification)
      if (context.options?.simulateCaptcha) {
        logStep('RULE ENFORCED: CAPTCHA detected on application portal. Never bypassing CAPTCHA.');
        return {
          applicationId: context.applicationId,
          status: 'HUMAN_INTERVENTION_REQUIRED',
          interventionReason: 'CAPTCHA',
          interventionDescription: 'CAPTCHA challenge encountered on employer portal. Human intervention required.',
          screenshots,
          logs,
          executionTimeMs: Date.now() - startTime,
        };
      }

      if (context.options?.simulateMfa) {
        logStep('RULE ENFORCED: MFA / OTP challenge detected on employer portal. Never bypassing MFA.');
        return {
          applicationId: context.applicationId,
          status: 'HUMAN_INTERVENTION_REQUIRED',
          interventionReason: 'MFA',
          interventionDescription: 'Employer application portal requires candidate MFA/OTP verification code.',
          screenshots,
          logs,
          executionTimeMs: Date.now() - startTime,
        };
      }

      if (context.options?.simulateUnknownQuestion) {
        logStep('RULE ENFORCED: Unknown screening question detected without candidate answer. Never fabricating candidate info.');
        return {
          applicationId: context.applicationId,
          status: 'HUMAN_INTERVENTION_REQUIRED',
          interventionReason: 'UNKNOWN_QUESTION',
          interventionDescription: 'Mandatory screening question has no verified answer in candidate profile.',
          screenshots,
          logs,
          executionTimeMs: Date.now() - startTime,
        };
      }

      // Step 1: Render / Navigate to simulated application portal HTML in Playwright page
      const mockPortalHtml = `
        <!DOCTYPE html>
        <html>
        <head>
          <title>${context.company} - Job Application</title>
          <style>
            body { font-family: sans-serif; padding: 20px; background: #f4f6f8; }
            .card { background: white; padding: 20px; border-radius: 8px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            input, textarea { width: 100%; margin-bottom: 12px; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
            button { background: #2563EB; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
          </style>
        </head>
        <body>
          <div class="card">
            <h1>Apply for ${context.jobTitle}</h1>
            <h2>${context.company}</h2>
            <form id="app-form">
              <label>Full Name</label>
              <input id="fullName" name="fullName" type="text" value="${context.candidate.fullName}" required />
              <label>Email</label>
              <input id="email" name="email" type="email" value="${context.candidate.email}" required />
              <label>Phone</label>
              <input id="phone" name="phone" type="tel" value="${context.candidate.phone}" required />
              <label>Cover Letter</label>
              <textarea id="coverLetter" name="coverLetter" rows="4">${context.coverLetter || ''}</textarea>
              <div id="confirmation-banner" style="display:none; color: green; font-weight: bold; margin-top: 15px;">
                Application Submitted Successfully! ID: CONF-${Date.now()}
              </div>
              <button id="submitBtn" type="button" onclick="document.getElementById('confirmation-banner').style.display='block'; document.getElementById('submitBtn').style.display='none';">
                Submit Application
              </button>
            </form>
          </div>
        </body>
        </html>
      `;

      await page.setContent(mockPortalHtml);
      logStep('Loaded employer portal application form.');

      // Check for live intervention elements
      const interventionCheck = await this.detectIntervention(page);
      if (interventionCheck.required) {
        logStep(`Intervention required: ${interventionCheck.reason} - ${interventionCheck.description}`);
        return {
          applicationId: context.applicationId,
          status: 'HUMAN_INTERVENTION_REQUIRED',
          interventionReason: interventionCheck.reason,
          interventionDescription: interventionCheck.description,
          screenshots,
          logs,
          executionTimeMs: Date.now() - startTime,
        };
      }

      // Fill and verify form fields
      await page.fill('#fullName', context.candidate.fullName);
      await page.fill('#email', context.candidate.email);
      await page.fill('#phone', context.candidate.phone);
      if (context.coverLetter) {
        await page.fill('#coverLetter', context.coverLetter);
      }
      logStep('Populated verified candidate contact details and tailored cover letter.');

      // Step 2: Fill screening questions
      for (const sq of context.screeningAnswers) {
        if (sq.confidence === 'UNKNOWN' || !sq.answer) {
          logStep(`Zero-fabrication rule: Question "${sq.question}" requires candidate verification. Halting automated submission.`);
          return {
            applicationId: context.applicationId,
            status: 'HUMAN_INTERVENTION_REQUIRED',
            interventionReason: 'UNKNOWN_QUESTION',
            interventionDescription: `Unanswered question requiring candidate input: "${sq.question}"`,
            screenshots,
            logs,
            executionTimeMs: Date.now() - startTime,
          };
        }
      }

      // Step 3: Trigger submission
      await page.click('#submitBtn');
      await page.waitForSelector('#confirmation-banner', { timeout: 5000 });
      logStep('Submitted application form and received confirmation banner.');

      const confirmationId = `CONF-${context.company.replace(/\s+/g, '').toUpperCase()}-${Date.now()}`;

      return {
        applicationId: context.applicationId,
        status: 'SUCCESS',
        confirmationId,
        screenshots,
        logs,
        executionTimeMs: Date.now() - startTime,
      };
    } catch (err: any) {
      logStep(`Execution error during application submission: ${err.message}`);
      return {
        applicationId: context.applicationId,
        status: 'FAILED',
        error: err.message,
        screenshots,
        logs,
        executionTimeMs: Date.now() - startTime,
      };
    }
  }
}
