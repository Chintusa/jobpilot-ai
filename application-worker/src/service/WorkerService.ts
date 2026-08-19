import { BrowserManager } from '../browser/BrowserManager';
import { ApplicationAdapterRegistry } from '../adapters/ApplicationAdapterRegistry';
import { ApplicationContext, ApplicationResult } from '../types';
import { logger } from '../logger';

export class WorkerService {
  private browserManager: BrowserManager;
  private adapterRegistry: ApplicationAdapterRegistry;

  constructor(
    browserManager: BrowserManager = new BrowserManager(),
    adapterRegistry: ApplicationAdapterRegistry = new ApplicationAdapterRegistry()
  ) {
    this.browserManager = browserManager;
    this.adapterRegistry = adapterRegistry;
  }

  async processApplication(context: ApplicationContext): Promise<ApplicationResult> {
    const startTime = Date.now();
    logger.info(`Processing application: id=${context.applicationId}, company=${context.company}, job=${context.jobTitle}`);

    const adapter = this.adapterRegistry.getAdapter(context.jobUrl, context.sourceName);
    if (!adapter) {
      logger.error(`No suitable adapter found for jobUrl=${context.jobUrl}`);
      return {
        applicationId: context.applicationId,
        status: 'HUMAN_INTERVENTION_REQUIRED',
        interventionReason: 'UNSUPPORTED_FLOW',
        interventionDescription: `No automated adapter available for employer portal: ${context.company}`,
        screenshots: [],
        logs: [`[${new Date().toISOString()}] No adapter found for URL: ${context.jobUrl}`],
        executionTimeMs: Date.now() - startTime,
      };
    }

    const headless = context.options?.headless !== false;
    let contextBundle;

    try {
      contextBundle = await this.browserManager.createContext(headless);
      const { page, context: browserContext } = contextBundle;

      // Execute adapter workflow
      const result = await adapter.apply(page, context);

      // If intervention required or failed, take screenshot for audit trail
      if (result.status === 'HUMAN_INTERVENTION_REQUIRED' || result.status === 'FAILED') {
        const screenshotPath = await this.browserManager.captureScreenshot(
          page,
          `intervention_${context.applicationId}`
        );
        if (screenshotPath) {
          result.screenshots.push(screenshotPath);
        }
      }

      await browserContext.close();
      return result;
    } catch (err: any) {
      logger.error(`Unhandled error during application processing: ${err.message}`, { error: err });
      return {
        applicationId: context.applicationId,
        status: 'FAILED',
        error: err.message,
        screenshots: [],
        logs: [`[${new Date().toISOString()}] Unhandled worker error: ${err.message}`],
        executionTimeMs: Date.now() - startTime,
      };
    }
  }

  async shutdown(): Promise<void> {
    await this.browserManager.closeBrowser();
  }
}
